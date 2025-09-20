package com.hospital.async;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.HospitalDetailApiCaller;
import com.hospital.dto.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import com.hospital.parser.HospitalDetailApiParser;
import com.hospital.repository.HospitalDetailApiRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HospitalDetailAsyncRunner {
	private final RateLimiter rateLimiter = RateLimiter.create(10.0); // 초당 10건 제한
	private final Executor executor; // 병렬 실행을 위한 스레드풀

	private final HospitalDetailApiCaller apiCaller;
	private final HospitalDetailApiParser parser;
	private final HospitalDetailApiRepository hospitalDetailApiRepository;

	private final AtomicInteger completedCount = new AtomicInteger(0);
	private final AtomicInteger failedCount = new AtomicInteger(0);
	private final AtomicInteger insertedCount = new AtomicInteger(0);
	private final AtomicInteger updatedCount = new AtomicInteger(0);
	private int totalCount = 0;

	private static final int BATCH_SIZE = 100;
	private static final int CHUNK_SIZE = 100; // 병원코드 청크 단위

	@Autowired
	public HospitalDetailAsyncRunner(HospitalDetailApiCaller apiCaller, HospitalDetailApiParser parser,
			HospitalDetailApiRepository hospitalDetailApiRepository, @Qualifier("apiExecutor") Executor executor) {
		this.apiCaller = apiCaller;
		this.parser = parser;
		this.hospitalDetailApiRepository = hospitalDetailApiRepository;
		this.executor = executor;
	}
	
	@Async("apiExecutor")
	public void runBatchAsync(List<String> hospitalCodes) {
	    log.info("멀티스레드 배치 시작: {}건", hospitalCodes.size());

	    try {
	        Map<String, HospitalDetail> existingMap = loadExistingDetails(hospitalCodes);
	        List<List<String>> partitions = partitionList(hospitalCodes, CHUNK_SIZE);

	        Set<String> processedCodes = new HashSet<>();

	        List<CompletableFuture<Void>> futures = partitions.stream()
	                .map(chunk -> CompletableFuture.runAsync(() -> processChunk(chunk, existingMap, processedCodes), executor))
	                .toList();

	        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

	        log.info("모든 청크 처리 완료: 완료 {}, 실패 {}, 신규 {}, 수정 {}", 
	                completedCount.get(), failedCount.get(), insertedCount.get(), updatedCount.get());

	        // 삭제 처리
	        deleteObsoleteDetails(processedCodes);

	    } catch (Exception e) {
	        failedCount.addAndGet(hospitalCodes.size());
	        log.error("전체 배치 실패: {}", e.getMessage(), e);
	    }
	}



	private void processChunk(List<String> chunk, Map<String, HospitalDetail> existingMap, Set<String> processedCodes) {
	    List<HospitalDetail> toInsert = new ArrayList<>();
	    List<HospitalDetail> toUpdate = new ArrayList<>();

	    for (String hospitalCode : chunk) {
	        rateLimiter.acquire();

	        try {
	            // API 호출
	            String queryParams = "ykiho=" + hospitalCode;
	            HospitalDetailApiResponse response = apiCaller.callApi(queryParams);

	            // 파싱
	            List<HospitalDetail> parsed = parser.parse(response, hospitalCode);
	            if (!parsed.isEmpty()) {
	                for (HospitalDetail newDetail : parsed) {
	                    newDetail.setHospitalCode(hospitalCode);

	                    HospitalDetail existing = existingMap.get(hospitalCode);
	                    if (existing != null) {
	                        updateDetailFields(existing, newDetail);
	                        toUpdate.add(existing);
	                    } else {
	                        toInsert.add(newDetail);
	                    }
	                }
	            }

	            completedCount.incrementAndGet();
	            processedCodes.add(hospitalCode); // 성공 처리된 코드 기록

	        } catch (Exception e) {
	            failedCount.incrementAndGet();
	            log.error("API 호출 실패: {}", hospitalCode, e);
	        }

	        if (toInsert.size() + toUpdate.size() >= BATCH_SIZE) {
	            saveBatchAndClear(toInsert, toUpdate);
	        }
	    }

	    if (!toInsert.isEmpty() || !toUpdate.isEmpty()) {
	        saveBatchAndClear(toInsert, toUpdate);
	    }
	}
	
	private void deleteObsoleteDetails(Set<String> processedCodes) {
	    List<String> allDbCodes = hospitalDetailApiRepository.findAllDistinctHospitalCodes();
	    List<String> toDelete = allDbCodes.stream()
	                                      .filter(code -> !processedCodes.contains(code))
	                                      .toList();

	    if (!toDelete.isEmpty()) {
	        hospitalDetailApiRepository.deleteAllById(toDelete);
	        log.info("폐업/삭제 처리 완료: {}건 삭제", toDelete.size());
	    }
	}

	private int[] saveBatchAndClear(List<HospitalDetail> toInsert, List<HospitalDetail> toUpdate) {
		int inserted = 0, updated = 0;

		if (!toInsert.isEmpty()) {
			hospitalDetailApiRepository.saveAll(toInsert);
			inserted = toInsert.size();
			insertedCount.addAndGet(inserted);
			toInsert.clear();
		}

		if (!toUpdate.isEmpty()) {
			hospitalDetailApiRepository.saveAll(toUpdate);
			updated = toUpdate.size();
			updatedCount.addAndGet(updated);
			toUpdate.clear();
		}

		return new int[] { inserted, updated };
	}

	private Map<String, HospitalDetail> loadExistingDetails(List<String> hospitalCodes) {
		List<HospitalDetail> existingDetails = hospitalDetailApiRepository.findByHospitalCodeIn(hospitalCodes);
		return existingDetails.stream().collect(Collectors.toMap(HospitalDetail::getHospitalCode, Function.identity()));
	}

	private List<List<String>> partitionList(List<String> list, int size) {
		List<List<String>> partitions = new ArrayList<>();
		for (int i = 0; i < list.size(); i += size) {
			partitions.add(list.subList(i, Math.min(i + size, list.size())));
		}
		return partitions;
	}

	



	// 필드별 업데이트
	private void updateDetailFields(HospitalDetail existing, HospitalDetail newData) {
		if (!Objects.equals(existing.getEmyDayYn(), newData.getEmyDayYn())) {
			existing.setEmyDayYn(newData.getEmyDayYn());
		}
		if (!Objects.equals(existing.getEmyNightYn(), newData.getEmyNightYn())) {
			existing.setEmyNightYn(newData.getEmyNightYn());
		}

		if (!Objects.equals(existing.getParkQty(), newData.getParkQty())) {
			existing.setParkQty(newData.getParkQty());
		}
		if (!Objects.equals(existing.getParkXpnsYn(), newData.getParkXpnsYn())) {
			existing.setParkXpnsYn(newData.getParkXpnsYn());
		}

		if (!Objects.equals(existing.getLunchWeek(), newData.getLunchWeek())) {
			existing.setLunchWeek(newData.getLunchWeek());
		}
		if (!Objects.equals(existing.getRcvWeek(), newData.getRcvWeek())) {
			existing.setRcvWeek(newData.getRcvWeek());
		}
		if (!Objects.equals(existing.getRcvSat(), newData.getRcvSat())) {
			existing.setRcvSat(newData.getRcvSat());
		}

		//if (!Objects.equals(existing.getNoTrmtHoli(), newData.getNoTrmtHoli())) {
			//existing.setNoTrmtHoli(newData.getNoTrmtHoli());
		//}
		//if (!Objects.equals(existing.getNoTrmtSun(), newData.getNoTrmtSun())) {
			//existing.setNoTrmtSun(newData.getNoTrmtSun());
		//}

		if (!Objects.equals(existing.getTrmtMonStart(), newData.getTrmtMonStart())) {
			existing.setTrmtMonStart(newData.getTrmtMonStart());
		}
		if (!Objects.equals(existing.getTrmtMonEnd(), newData.getTrmtMonEnd())) {
			existing.setTrmtMonEnd(newData.getTrmtMonEnd());
		}
		if (!Objects.equals(existing.getTrmtTueStart(), newData.getTrmtTueStart())) {
			existing.setTrmtTueStart(newData.getTrmtTueStart());
		}
		if (!Objects.equals(existing.getTrmtTueEnd(), newData.getTrmtTueEnd())) {
			existing.setTrmtTueEnd(newData.getTrmtTueEnd());
		}
		if (!Objects.equals(existing.getTrmtWedStart(), newData.getTrmtWedStart())) {
			existing.setTrmtWedStart(newData.getTrmtWedStart());
		}
		if (!Objects.equals(existing.getTrmtWedEnd(), newData.getTrmtWedEnd())) {
			existing.setTrmtWedEnd(newData.getTrmtWedEnd());
		}
		if (!Objects.equals(existing.getTrmtThurStart(), newData.getTrmtThurStart())) {
			existing.setTrmtThurStart(newData.getTrmtThurStart());
		}
		if (!Objects.equals(existing.getTrmtThurEnd(), newData.getTrmtThurEnd())) {
			existing.setTrmtThurEnd(newData.getTrmtThurEnd());
		}
		if (!Objects.equals(existing.getTrmtFriStart(), newData.getTrmtFriStart())) {
			existing.setTrmtFriStart(newData.getTrmtFriStart());
		}
		if (!Objects.equals(existing.getTrmtFriEnd(), newData.getTrmtFriEnd())) {
			existing.setTrmtFriEnd(newData.getTrmtFriEnd());
		}
		if (!Objects.equals(existing.getTrmtSatStart(), newData.getTrmtSatStart())) {
			existing.setTrmtSatStart(newData.getTrmtSatStart());
		}
		if (!Objects.equals(existing.getTrmtSatEnd(), newData.getTrmtSatEnd())) {
			existing.setTrmtSatEnd(newData.getTrmtSatEnd());
		}
		if (!Objects.equals(existing.getTrmtSunStart(), newData.getTrmtSunStart())) {
			existing.setTrmtSunStart(newData.getTrmtSunStart());
		}
		if (!Objects.equals(existing.getTrmtSunEnd(), newData.getTrmtSunEnd())) {
			existing.setTrmtSunEnd(newData.getTrmtSunEnd());
		}
	}

	// 상태 관리 메서드들
	public void resetCounter() {
		completedCount.set(0);
		failedCount.set(0);
		insertedCount.set(0);
		updatedCount.set(0);
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		completedCount.set(0);
		failedCount.set(0);
		insertedCount.set(0);
		updatedCount.set(0);
	}

	public int getCompletedCount() {
		return completedCount.get();
	}

	public int getFailedCount() {
		return failedCount.get();
	}

	public int getInsertedCount() {
		return insertedCount.get();
	}

	public int getUpdatedCount() {
		return updatedCount.get();
	}
}

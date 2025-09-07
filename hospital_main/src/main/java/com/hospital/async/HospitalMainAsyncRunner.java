package com.hospital.async;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Autowired;
import com.hospital.caller.HospitalMainApiCaller;
import com.hospital.parser.HospitalMainApiParser;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.dto.HospitalMainApiResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.config.RegionConfig;

@Service
@Slf4j
public class HospitalMainAsyncRunner {
	private final RateLimiter rateLimiter = RateLimiter.create(2.0);
	private final AtomicInteger completedCount = new AtomicInteger(0);
	private final AtomicInteger failedCount = new AtomicInteger(0);
	private final AtomicInteger updatedCount = new AtomicInteger(0);
	private final AtomicInteger insertedCount = new AtomicInteger(0);
	private int totalCount = 0;

	private final HospitalMainApiCaller apiCaller;
	private final HospitalMainApiParser parser;
	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final RegionConfig regionConfig;
	private static final int BATCH_SIZE = 100;

	@Autowired
	public HospitalMainAsyncRunner(HospitalMainApiCaller apiCaller, HospitalMainApiParser parser,
			HospitalMainApiRepository hospitalMainApiRepository, RegionConfig regionConfig) {
		this.apiCaller = apiCaller;
		this.parser = parser;
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.regionConfig = regionConfig;
	}

	@Async("apiExecutor")
	public void runAsync(String sgguCd) {
		rateLimiter.acquire();
		try {
			log.info("지역코드 {} 처리 시작", regionConfig.getDistrictName(sgguCd));

			// 유효성 검사
			if (sgguCd == null || sgguCd.trim().isEmpty()) {
				throw new IllegalArgumentException("지역코드가 비어있습니다");
			}

			int totalSaved = 0;
			int pageNo = 1;
			int numOfRows = 1000;
			boolean hasMorePages = true;

			List<HospitalMain> batchList = new ArrayList<>();

			while (hasMorePages) {

				String queryParams = String.format("sgguCd=%s&pageNo=%s&numOfRows=%s", sgguCd, pageNo, numOfRows);
				HospitalMainApiResponse response = apiCaller.callApi(queryParams);

				List<HospitalMain> hospitals = parser.parseHospitals(response);

				if (hospitals.isEmpty()) {
					log.info("지역{} 페이지{} : 더이상 데이터 없음", regionConfig.getDistrictName(sgguCd), pageNo);

					break;
				}
				batchList.addAll(hospitals);
				log.info("지역 {} 페이지 {}: {}건 수집 (총 누적: {}건)", regionConfig.getDistrictName(sgguCd), pageNo,
						hospitals.size(), batchList.size());

				if (batchList.size() >= BATCH_SIZE) {
					UpsertResult result = upsertHospital(batchList);
					totalSaved += result.inserted + result.updated;

					insertedCount.addAndGet(result.inserted);
					updatedCount.addAndGet(result.updated);

					log.info("지역 {} 중간 배치 처리: {}건 처리 (신규:{}, 수정:{})", regionConfig.getDistrictName(sgguCd),
							result.inserted + result.updated, result.inserted, result.updated);

					batchList = new ArrayList<>();
				}

				hasMorePages = hospitals.size() >= numOfRows;
				pageNo++;

				// 페이지 간 대기
				Thread.sleep(1000);
			}

			if (!batchList.isEmpty()) {
				log.info("지역 {} 데이터 수집완료. 총 {} 건 일괄 처리 시작", regionConfig.getDistrictName(sgguCd), batchList.size());
				UpsertResult result = upsertHospital(batchList);
				totalSaved += result.inserted + result.updated;

				insertedCount.addAndGet(result.inserted);
				updatedCount.addAndGet(result.updated);

				log.info("지역 {} 일괄 처리 완료: {}건 처리 (신규:{}, 수정:{})", regionConfig.getDistrictName(sgguCd),
						result.inserted + result.updated, result.inserted, result.updated);
			}

			completedCount.incrementAndGet();
			log.info("지역 {} 처리 완료: 총 {}건 저장", regionConfig.getDistrictName(sgguCd), totalSaved);

		} catch (Exception e) {
			failedCount.incrementAndGet();
			log.error("지역 코드 {} 처리 실패: {}", regionConfig.getDistrictName(sgguCd), e.getMessage());
		}

	}

	@Transactional
	public UpsertResult upsertHospital(List<HospitalMain> hospitals) {
	    int updated = 0;
	    int inserted = 0;

	    // 배치 조회: 모든 병원 코드를 한 번에 조회
	    List<String> hospitalCodes = hospitals.stream()
	        .map(HospitalMain::getHospitalCode)
	        .collect(Collectors.toList());
	        
	    Map<String, HospitalMain> existingMap = hospitalMainApiRepository
	        .findByHospitalCodeIn(hospitalCodes)
	        .stream()
	        .collect(Collectors.toMap(HospitalMain::getHospitalCode, Function.identity()));

	    // 개별 처리 (이제 DB 조회 없이 Map에서 조회)
	    for (HospitalMain newHospital : hospitals) {
	        try {
	            HospitalMain existing = existingMap.get(newHospital.getHospitalCode());
	            
	            if (existing != null) {
	                updateHospital(existing, newHospital);
	                hospitalMainApiRepository.save(existing);
	                updated++;
	            } else {
	                hospitalMainApiRepository.save(newHospital);
	                inserted++;
	            }
	        } catch (Exception e) {
	            log.warn("병원 {} UPSERT 실패", newHospital.getHospitalCode(), e.getMessage());
	        }
	    }
	    return new UpsertResult(inserted, updated);
	}

	private void updateHospital(HospitalMain existing, HospitalMain newData) {
		existing.setHospitalName(newData.getHospitalName());
		existing.setProvinceName(newData.getProvinceName());
		existing.setDistrictName(newData.getDistrictName());
		existing.setHospitalAddress(newData.getHospitalAddress());
		existing.setHospitalTel(newData.getHospitalTel());
		existing.setHospitalHomepage(newData.getHospitalHomepage());
		existing.setDoctorNum(newData.getDoctorNum());
		existing.setCoordinateX(newData.getCoordinateX());
		existing.setCoordinateY(newData.getCoordinateY());

	}

	private static class UpsertResult {
		final int inserted; // 신규 추가된 수
		final int updated; // 수정된 수

		UpsertResult(int inserted, int updated) {
			this.inserted = inserted;
			this.updated = updated;
		}
	}

	public int getCompletedCount() {
		return completedCount.get();
	}

	public int getFailedCount() {
		return failedCount.get();
	}

	public void resetCounter() {
		completedCount.set(0);
		failedCount.set(0);
	}

	public int getInsertedCount() {
		return insertedCount.get();
	}

	public int getUpdatedCount() {
		return updatedCount.get();
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		completedCount.set(0);
		failedCount.set(0);
		insertedCount.set(0);
		updatedCount.set(0);
	}

}

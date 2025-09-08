package com.hospital.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.HospitalDetailApiCaller;
import com.hospital.dto.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.HospitalMain;
import com.hospital.parser.HospitalDetailApiParser;
import com.hospital.repository.HospitalDetailApiRepository;
import com.hospital.repository.HospitalMainApiRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HospitalDetailAsyncRunner {
	private final RateLimiter rateLimiter = RateLimiter.create(20.0); // 초당 20건 제한

	// 의존성 주입: API 호출, 파싱, 저장을 담당하는 객체들
	private final HospitalDetailApiCaller apiCaller;
	private final HospitalDetailApiParser parser;
	private final HospitalDetailApiRepository hospitalDetailApiRepository;
	private final HospitalMainApiRepository hospitalMainApiRepository;

	// 처리 상태 추적용 카운터
	private final AtomicInteger completedCount = new AtomicInteger(0); // 성공
	private final AtomicInteger failedCount = new AtomicInteger(0); // 실패
	private final AtomicInteger updatedCount = new AtomicInteger(0);
	private final AtomicInteger insertedCount = new AtomicInteger(0);
	private int totalCount = 0; // 전체 병원 수
	private static final int BATCH_SIZE = 100;

	@Autowired
	public HospitalDetailAsyncRunner(HospitalDetailApiCaller apiCaller, HospitalDetailApiParser parser,
			HospitalDetailApiRepository hospitalDetailApiRepository,
			HospitalMainApiRepository hospitalMainRepository) {
		this.apiCaller = apiCaller;
		this.parser = parser;
		this.hospitalDetailApiRepository = hospitalDetailApiRepository;
		this.hospitalMainApiRepository = hospitalMainRepository;
	}
	// 병원코드 단위 비동기 처리
	@Async("apiExecutor")
    public void runBatchAsync(List<String> hospitalCodes) {
        log.info("배치 처리 시작: {}건", hospitalCodes.size());
        
        try {
            // 1. 기존 데이터 배치 조회 (1번 DB 쿼리)
            Map<String, HospitalDetail> existingMap = loadExistingDetails(hospitalCodes);
            
            // 2. 실시간 배치 처리용 리스트
            List<HospitalDetail> toInsert = new ArrayList<>();
            List<HospitalDetail> toUpdate = new ArrayList<>();
            
            int apiSuccessCount = 0;
            int apiFailureCount = 0;
            int insertedTotal = 0;
            int updatedTotal = 0;
            
            for (String hospitalCode : hospitalCodes) {
                rateLimiter.acquire(); // Rate limiting
                
                try {
                    // API 호출
                    String queryParams = String.format("ykiho=%s", hospitalCode);
                    HospitalDetailApiResponse response = apiCaller.callApi(queryParams);
                    
                    // 파싱
                    List<HospitalDetail> parsed = parser.parse(response, hospitalCode);
                    
                    if (!parsed.isEmpty()) {
                        // 각 파싱된 데이터 처리
                        for (HospitalDetail newDetail : parsed) {
                            newDetail.setHospitalCode(hospitalCode);
                            
                            HospitalDetail existing = existingMap.get(hospitalCode);
                            if (existing != null) {
                                // 기존 데이터 업데이트
                                updateDetails(existing, newDetail);
                                toUpdate.add(existing);
                            } else {
                                // 신규 데이터 삽입
                                toInsert.add(newDetail);
                            }
                        }
                    }
                    
                    apiSuccessCount++;
                    
                    // ✅ if 방식: 일정 개수 쌓이면 중간 저장 (메모리 효율)
                    if (toInsert.size() + toUpdate.size() >= BATCH_SIZE) {
                        int[] results = saveBatchAndClear(toInsert, toUpdate);
                        insertedTotal += results[0];
                        updatedTotal += results[1];
                        
                        log.info("중간 배치 저장: 신규 {}건, 수정 {}건", results[0], results[1]);
                    }
                    
                } catch (Exception e) {
                    apiFailureCount++;
                    log.error("API 호출 실패: {}", hospitalCode, e);
                }
            }
            
            // 3. 마지막 남은 데이터 저장
            if (!toInsert.isEmpty() || !toUpdate.isEmpty()) {
                int[] results = saveBatchAndClear(toInsert, toUpdate);
                insertedTotal += results[0];
                updatedTotal += results[1];
                
                log.info("최종 배치 저장: 신규 {}건, 수정 {}건", results[0], results[1]);
            }
            
            // 4. 카운터 업데이트
            completedCount.addAndGet(apiSuccessCount);
            failedCount.addAndGet(apiFailureCount);
            insertedCount.addAndGet(insertedTotal);
            updatedCount.addAndGet(updatedTotal);
            
            log.info("배치 처리 완료: API 성공 {}, 실패 {}, 신규 {}, 수정 {}", 
                    apiSuccessCount, apiFailureCount, insertedTotal, updatedTotal);
                    
        } catch (Exception e) {
            failedCount.addAndGet(hospitalCodes.size());
            log.error("배치 처리 전체 실패: {}", e.getMessage(), e);
        }
    }
	 private int[] saveBatchAndClear(List<HospitalDetail> toInsert, List<HospitalDetail> toUpdate) {
	        int inserted = 0;
	        int updated = 0;
	        
	        if (!toInsert.isEmpty()) {
	            hospitalDetailApiRepository.saveAll(toInsert);
	            inserted = toInsert.size();
	            toInsert.clear(); // 메모리 해제
	        }
	        
	        if (!toUpdate.isEmpty()) {
	            hospitalDetailApiRepository.saveAll(toUpdate);
	            updated = toUpdate.size();
	            toUpdate.clear(); // 메모리 해제
	        }
	        
	        return new int[]{inserted, updated};
	    }

	 private Map<String, HospitalDetail> loadExistingDetails(List<String> hospitalCodes) {
	        List<HospitalDetail> existingDetails = hospitalDetailApiRepository.findByHospitalCodeIn(hospitalCodes);
	        
	        return existingDetails.stream()
	                .collect(Collectors.toMap(HospitalDetail::getHospitalCode, Function.identity()));
	    }
	
	private void updateDetails(HospitalDetail existing, HospitalDetail newData) {
	    // 응급실 운영 정보
	    existing.setEmyDayYn(newData.getEmyDayYn());
	    existing.setEmyNightYn(newData.getEmyNightYn());
	    
	    // 주차 정보
	    existing.setParkQty(newData.getParkQty());
	    existing.setParkXpnsYn(newData.getParkXpnsYn());
	    
	    // 점심시간 및 접수 정보
	    existing.setLunchWeek(newData.getLunchWeek());
	    existing.setRcvWeek(newData.getRcvWeek());
	    existing.setRcvSat(newData.getRcvSat());
	    
	    // 휴무일 정보
	    existing.setNoTrmtHoli(newData.getNoTrmtHoli());
	    existing.setNoTrmtSun(newData.getNoTrmtSun());
	    
	    // 월요일 진료시간
	    existing.setTrmtMonStart(newData.getTrmtMonStart());
	    existing.setTrmtMonEnd(newData.getTrmtMonEnd());
	    
	    // 화요일 진료시간
	    existing.setTrmtTueStart(newData.getTrmtTueStart());
	    existing.setTrmtTueEnd(newData.getTrmtTueEnd());
	    
	    // 수요일 진료시간
	    existing.setTrmtWedStart(newData.getTrmtWedStart());
	    existing.setTrmtWedEnd(newData.getTrmtWedEnd());
	    
	    // 목요일 진료시간
	    existing.setTrmtThurStart(newData.getTrmtThurStart());
	    existing.setTrmtThurEnd(newData.getTrmtThurEnd());
	    
	    // 금요일 진료시간
	    existing.setTrmtFriStart(newData.getTrmtFriStart());
	    existing.setTrmtFriEnd(newData.getTrmtFriEnd());
	    
	    // 토요일 진료시간
	    existing.setTrmtSatStart(newData.getTrmtSatStart());
	    existing.setTrmtSatEnd(newData.getTrmtSatEnd());
	    
	    // 일요일 진료시간
	    existing.setTrmtSunStart(newData.getTrmtSunStart());
	    existing.setTrmtSunEnd(newData.getTrmtSunEnd());
	}

	// 진행 상태 초기화
	public void resetCounter() {
		completedCount.set(0);
		failedCount.set(0);
		insertedCount.set(0);
		updatedCount.set(0);
	}

	// 총 작업 수 설정 및 카운터 초기화
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		completedCount.set(0);
		failedCount.set(0);
		insertedCount.set(0);
		updatedCount.set(0);
	}

	// 현재까지 완료된 작업 수
	public int getCompletedCount() {
		return completedCount.get();
	}

	// 현재까지 실패한 작업 수
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
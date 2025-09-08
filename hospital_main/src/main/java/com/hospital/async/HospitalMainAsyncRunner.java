package com.hospital.async;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

          
            List<HospitalMain> toInsert = new ArrayList<>();
            List<HospitalMain> toUpdate = new ArrayList<>();
            int insertedTotal = 0;
            int updatedTotal = 0;

            while (hasMorePages) {
                String queryParams = String.format("sgguCd=%s&pageNo=%s&numOfRows=%s", sgguCd, pageNo, numOfRows);
                HospitalMainApiResponse response = apiCaller.callApi(queryParams);

                List<HospitalMain> hospitals = parser.parseHospitals(response);

                if (hospitals.isEmpty()) {
                    log.info("지역{} 페이지{} : 더이상 데이터 없음", regionConfig.getDistrictName(sgguCd), pageNo);
                    break;
                }

                // ✅ 배치 조회: API에서 가져온 병원들의 기존 데이터 조회
                List<String> hospitalCodes = hospitals.stream()
                    .map(HospitalMain::getHospitalCode)
                    .collect(Collectors.toList());
                    
                Map<String, HospitalMain> existingMap = hospitalMainApiRepository
                    .findByHospitalCodeIn(hospitalCodes)
                    .stream()
                    .collect(Collectors.toMap(HospitalMain::getHospitalCode, Function.identity()));

               
                for (HospitalMain newHospital : hospitals) {
                    HospitalMain existing = existingMap.get(newHospital.getHospitalCode());
                    
                    if (existing != null) {
                        // 기존 데이터 업데이트
                        updateHospital(existing, newHospital);
                        toUpdate.add(existing);
                    } else {
                        // 신규 데이터 삽입
                        toInsert.add(newHospital);
                    }
                }

                log.info("지역 {} 페이지 {}: {}건 수집 (신규: {}, 수정: {})", 
                        regionConfig.getDistrictName(sgguCd), pageNo, hospitals.size(),
                        hospitals.size() - existingMap.size(), existingMap.size());

               
                if (toInsert.size() + toUpdate.size() >= BATCH_SIZE) {
                    int[] results = saveBatchAndClear(toInsert, toUpdate);
                    insertedTotal += results[0];
                    updatedTotal += results[1];
                    totalSaved += results[0] + results[1];

                    log.info("지역 {} 중간 배치 저장: 신규 {}건, 수정 {}건", 
                            regionConfig.getDistrictName(sgguCd), results[0], results[1]);
                }

                hasMorePages = hospitals.size() >= numOfRows;
                pageNo++;

                // 페이지 간 대기
                Thread.sleep(1000);
            }

      
            if (!toInsert.isEmpty() || !toUpdate.isEmpty()) {
                int[] results = saveBatchAndClear(toInsert, toUpdate);
                insertedTotal += results[0];
                updatedTotal += results[1];
                totalSaved += results[0] + results[1];

                log.info("지역 {} 최종 배치 저장: 신규 {}건, 수정 {}건", 
                        regionConfig.getDistrictName(sgguCd), results[0], results[1]);
            }

        
            completedCount.incrementAndGet();
            insertedCount.addAndGet(insertedTotal);
            updatedCount.addAndGet(updatedTotal);

            log.info("지역 {} 처리 완료: 총 {}건 저장 (신규: {}, 수정: {})", 
                    regionConfig.getDistrictName(sgguCd), totalSaved, insertedTotal, updatedTotal);

        } catch (Exception e) {
            failedCount.incrementAndGet();
            log.error("지역 코드 {} 처리 실패: {}", regionConfig.getDistrictName(sgguCd), e.getMessage());
        }
    }

 
    private int[] saveBatchAndClear(List<HospitalMain> toInsert, List<HospitalMain> toUpdate) {
        int inserted = 0;
        int updated = 0;
        
        if (!toInsert.isEmpty()) {
            hospitalMainApiRepository.saveAll(toInsert);  // 배치 저장
            inserted = toInsert.size();
            toInsert.clear(); // 메모리 해제
        }
        
        if (!toUpdate.isEmpty()) {
            hospitalMainApiRepository.saveAll(toUpdate);  // 배치 저장
            updated = toUpdate.size();
            toUpdate.clear(); // 메모리 해제
        }
        
        return new int[]{inserted, updated};
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

    

    public int getCompletedCount() {
        return completedCount.get();
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    public void resetCounter() {
        completedCount.set(0);
        failedCount.set(0);
        insertedCount.set(0);
        updatedCount.set(0);
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
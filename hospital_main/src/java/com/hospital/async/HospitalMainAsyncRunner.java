package com.hospital.async;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final RateLimiter rateLimiter = RateLimiter.create(5.0);
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger updatedCount = new AtomicInteger(0);
    private final AtomicInteger insertedCount = new AtomicInteger(0);
    private final AtomicInteger deletedCount = new AtomicInteger(0); // ✅ 삭제 카운터 추가
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
    public void runAsync(String sidoCd) {
        rateLimiter.acquire();
        try {
            log.info("지역코드 {} 처리 시작", regionConfig.getSidoName(sidoCd));

            // 유효성 검사
            if (sidoCd == null || sidoCd.trim().isEmpty()) {
                throw new IllegalArgumentException("지역코드가 비어있습니다");
            }

            // ✅ 1. 해당 지역의 기존 데이터 전체 조회 (삭제 로직용)
            String sidoName = regionConfig.getSidoName(sidoCd);
            List<HospitalMain> existingHospitals = hospitalMainApiRepository.findByProvinceName(sidoName);
            Map<String, HospitalMain> existingMap = existingHospitals.stream()
                .collect(Collectors.toMap(HospitalMain::getHospitalCode, Function.identity()));
            
            Set<String> apiSuccessCodes = new HashSet<>(); // ✅ API에서 성공적으로 받아온 코드들

            int totalSaved = 0;
            int pageNo = 1;
            int numOfRows = 1000;
            boolean hasMorePages = true;

            List<HospitalMain> toInsert = new ArrayList<>();
            List<HospitalMain> toUpdate = new ArrayList<>();
            int insertedTotal = 0;
            int updatedTotal = 0;

            while (hasMorePages) {
                String queryParams = String.format("sidoCd=%s&pageNo=%s&numOfRows=%s", sidoCd, pageNo, numOfRows);
                HospitalMainApiResponse response = apiCaller.callApi(queryParams);

                List<HospitalMain> hospitals = parser.parseHospitals(response);

                if (hospitals.isEmpty()) {
                    log.info("지역{} 페이지{} : 더이상 데이터 없음", regionConfig.getSidoName(sidoCd), pageNo);
                    break;
                }

                // ✅ API에서 받아온 병원 코드들을 성공 목록에 추가
                hospitals.forEach(hospital -> 
                    apiSuccessCodes.add(hospital.getHospitalCode())
                );

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
                        regionConfig.getSidoName(sidoCd), pageNo, hospitals.size(),
                        hospitals.size() - (int)hospitals.stream()
                            .filter(h -> existingMap.containsKey(h.getHospitalCode()))
                            .count(),
                        (int)hospitals.stream()
                            .filter(h -> existingMap.containsKey(h.getHospitalCode()))
                            .count());

                // 배치 저장
                if (toInsert.size() + toUpdate.size() >= BATCH_SIZE) {
                    int[] results = saveBatchAndClear(toInsert, toUpdate);
                    insertedTotal += results[0];
                    updatedTotal += results[1];
                    totalSaved += results[0] + results[1];

                    log.info("지역 {} 중간 배치 저장: 신규 {}건, 수정 {}건", 
                            regionConfig.getSidoName(sidoCd), results[0], results[1]);
                }

                hasMorePages = hospitals.size() >= numOfRows;
                pageNo++;

                // 페이지 간 대기
                Thread.sleep(1000);
            }

            // ✅ 2. 마지막 남은 데이터 저장
            if (!toInsert.isEmpty() || !toUpdate.isEmpty()) {
                int[] results = saveBatchAndClear(toInsert, toUpdate);
                insertedTotal += results[0];
                updatedTotal += results[1];
                totalSaved += results[0] + results[1];

                log.info("지역 {} 최종 배치 저장: 신규 {}건, 수정 {}건", 
                        regionConfig.getSidoName(sidoCd), results[0], results[1]);
            }

            // ✅ 3. 삭제 로직: 해당 지역에서 API에 없는 병원들 삭제
            List<String> toDelete = existingMap.keySet().stream()
                .filter(code -> !apiSuccessCodes.contains(code))
                .collect(Collectors.toList());
                
            int deletedTotal = 0;
            if (!toDelete.isEmpty()) {
                hospitalMainApiRepository.deleteByHospitalCodeIn(toDelete);
                deletedTotal = toDelete.size();
                log.info("지역 {} 폐업/제외된 병원 삭제: {}건", 
                        regionConfig.getSidoName(sidoCd), deletedTotal);
            }

            // ✅ 4. 카운터 업데이트
            completedCount.incrementAndGet();
            insertedCount.addAndGet(insertedTotal);
            updatedCount.addAndGet(updatedTotal);
            deletedCount.addAndGet(deletedTotal); // 삭제 카운터 업데이트

            log.info("지역 {} 처리 완료: 총 {}건 저장 (신규: {}, 수정: {}, 삭제: {})", 
                    regionConfig.getDistrictName(sidoCd), totalSaved, insertedTotal, updatedTotal, deletedTotal);

        } catch (Exception e) {
            failedCount.incrementAndGet();
            log.error("지역 코드 {} 처리 실패: {}", regionConfig.getSidoName(sidoCd), e.getMessage());
        }
    }

    // ✅ 배치 저장 및 리스트 초기화 헬퍼 메서드
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

    // ✅ 필드별 업데이트
    private void updateHospital(HospitalMain existing, HospitalMain newData) {
        if (!Objects.equals(existing.getHospitalName(), newData.getHospitalName())) {
            existing.setHospitalName(newData.getHospitalName());
        }
        if (!Objects.equals(existing.getProvinceName(), newData.getProvinceName())) {
            existing.setProvinceName(newData.getProvinceName());
        }
        if (!Objects.equals(existing.getDistrictName(), newData.getDistrictName())) {
            existing.setDistrictName(newData.getDistrictName());
        }
        if (!Objects.equals(existing.getHospitalAddress(), newData.getHospitalAddress())) {
            existing.setHospitalAddress(newData.getHospitalAddress());
        }
        if (!Objects.equals(existing.getHospitalTel(), newData.getHospitalTel())) {
            existing.setHospitalTel(newData.getHospitalTel());
        }
        if (!Objects.equals(existing.getHospitalHomepage(), newData.getHospitalHomepage())) {
            existing.setHospitalHomepage(newData.getHospitalHomepage());
        }
        if (!Objects.equals(existing.getDoctorNum(), newData.getDoctorNum())) {
            existing.setDoctorNum(newData.getDoctorNum());
        }
        if (!Objects.equals(existing.getCoordinateX(), newData.getCoordinateX())) {
            existing.setCoordinateX(newData.getCoordinateX());
        }
        if (!Objects.equals(existing.getCoordinateY(), newData.getCoordinateY())) {
            existing.setCoordinateY(newData.getCoordinateY());
        }
    }

    // ✅ 상태 관리 메서드들
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

    public int getDeletedCount() { // ✅ 삭제 카운터 getter 추가
        return deletedCount.get();
    }

    public void resetCounter() {
        completedCount.set(0);
        failedCount.set(0);
        insertedCount.set(0);
        updatedCount.set(0);
        deletedCount.set(0); // ✅ 삭제 카운터 리셋 추가
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        completedCount.set(0);
        failedCount.set(0);
        insertedCount.set(0);
        updatedCount.set(0);
        deletedCount.set(0); // ✅ 삭제 카운터 리셋 추가
    }
}
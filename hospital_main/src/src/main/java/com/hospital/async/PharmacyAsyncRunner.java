package com.hospital.async;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.PharmacyApiCaller;
import com.hospital.config.RegionConfig;
import com.hospital.dto.OpenApiWrapper;
import com.hospital.entity.Pharmacy;
import com.hospital.parser.PharmacyApiParser;
import com.hospital.repository.PharmacyApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PharmacyAsyncRunner {

    private final RateLimiter rateLimiter = RateLimiter.create(2.0); // 초당 2회 제한
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger insertedCount = new AtomicInteger(0);
    private final AtomicInteger updatedCount = new AtomicInteger(0);
    private final AtomicInteger deletedCount = new AtomicInteger(0);
    private int totalCount = 0;

    private static final int BATCH_SIZE = 100;

    private final PharmacyApiCaller apiCaller;
    private final PharmacyApiParser parser;
    private final PharmacyApiRepository repository;
    private final RegionConfig regionConfig;

    @Autowired
    public PharmacyAsyncRunner(PharmacyApiCaller apiCaller,
                               PharmacyApiParser parser,
                               PharmacyApiRepository repository,
                               RegionConfig regionConfig) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.repository = repository;
        this.regionConfig = regionConfig;
    }

    @Async("apiExecutor")
    public void runAsync(String sgguCd) {
        rateLimiter.acquire();
        try {
            String districtName = regionConfig.getDistrictName(sgguCd);
            log.info("지역코드 {} 약국 처리 시작", districtName);

            if (sgguCd == null || sgguCd.trim().isEmpty()) {
                throw new IllegalArgumentException("지역코드가 비어있습니다");
            }

            // 기존 DB 조회 (삭제 처리용)
            List<Pharmacy> existingList = repository.findAll();;
            Map<String, Pharmacy> existingMap = existingList.stream()
                    .collect(Collectors.toMap(Pharmacy::getYkiho, Function.identity()));

            Set<String> apiSuccessCodes = new HashSet<>(); // API에서 가져온 유효한 약국 코드
            List<Pharmacy> toInsert = new ArrayList<>();
            List<Pharmacy> toUpdate = new ArrayList<>();
            int pageNo = 1;
            int numOfRows = 1000;
            boolean hasMorePages = true;
            int insertedTotal = 0;
            int updatedTotal = 0;

            while (hasMorePages) {
            	OpenApiWrapper.Body body = apiCaller.callApiByDistrict(sgguCd, pageNo, numOfRows);
            	if (body == null || body.getItems() == null || body.getItems().isEmpty()) {
            	    break; // 더 이상 데이터 없으면 종료
            	}

            	// XML → Entity 변환
            	List<Pharmacy> pharmacies = parser.parseToEntities(body.getItems());

                if (pharmacies.isEmpty()) {
                    log.info("지역 {} 페이지 {}: 더 이상 데이터 없음", districtName, pageNo);
                    break;
                }

                pharmacies.forEach(pharmacy -> apiSuccessCodes.add(pharmacy.getYkiho()));

                for (Pharmacy newPharmacy : pharmacies) {
                    Pharmacy existing = existingMap.get(newPharmacy.getYkiho());
                    if (existing != null) {
                        updatePharmacy(existing, newPharmacy);
                        toUpdate.add(existing);
                    } else {
                        toInsert.add(newPharmacy);
                    }
                }

                // 배치 저장
                if (toInsert.size() + toUpdate.size() >= BATCH_SIZE) {
                    int[] results = saveBatchAndClear(toInsert, toUpdate);
                    insertedTotal += results[0];
                    updatedTotal += results[1];
                    log.info("지역 {} 페이지 {} 중간 배치 저장: 신규 {}, 수정 {}", districtName, pageNo, results[0], results[1]);
                }

                hasMorePages = pharmacies.size() >= numOfRows;
                pageNo++;
                Thread.sleep(1000); // 페이지 간 대기
            }

            // 마지막 남은 배치 저장
            if (!toInsert.isEmpty() || !toUpdate.isEmpty()) {
                int[] results = saveBatchAndClear(toInsert, toUpdate);
                insertedTotal += results[0];
                updatedTotal += results[1];
            }

            // 삭제 처리: API에 없는 약국 제거
            List<String> toDelete = existingMap.keySet().stream()
                    .filter(code -> !apiSuccessCodes.contains(code))
                    .toList();

            int deletedTotal = 0;
            if (!toDelete.isEmpty()) {
                repository.deleteByYkihoIn(toDelete);
                deletedTotal = toDelete.size();
                log.info("지역 {} 폐업/제외 약국 삭제: {}건", districtName, deletedTotal);
            }

            // 카운터 업데이트
            completedCount.incrementAndGet();
            insertedCount.addAndGet(insertedTotal);
            updatedCount.addAndGet(updatedTotal);
            deletedCount.addAndGet(deletedTotal);

            log.info("지역 {} 처리 완료: 신규 {}, 수정 {}, 삭제 {}", districtName, insertedTotal, updatedTotal, deletedTotal);

        } catch (Exception e) {
            failedCount.incrementAndGet();
            log.error("지역코드 {} 약국 처리 실패: {}", sgguCd, e.getMessage(), e);
        }
    }

    private int[] saveBatchAndClear(List<Pharmacy> toInsert, List<Pharmacy> toUpdate) {
        int inserted = 0, updated = 0;
        if (!toInsert.isEmpty()) {
            repository.saveAll(toInsert);
            inserted = toInsert.size();
            toInsert.clear();
        }
        if (!toUpdate.isEmpty()) {
            repository.saveAll(toUpdate);
            updated = toUpdate.size();
            toUpdate.clear();
        }
        return new int[]{inserted, updated};
    }

    private void updatePharmacy(Pharmacy existing, Pharmacy newData) {
        if (!Objects.equals(existing.getName(), newData.getName())) {
            existing.setName(newData.getName());
        }
        if (!Objects.equals(existing.getAddress(), newData.getAddress())) {
            existing.setAddress(newData.getAddress());
        }
        if (!Objects.equals(existing.getPhone(), newData.getPhone())) {
            existing.setPhone(newData.getPhone());
        }
        if (!Objects.equals(existing.getLatitude(), newData.getLatitude())) {
            existing.setLatitude(newData.getLatitude());
        }
        if (!Objects.equals(existing.getLongitude(), newData.getLongitude())) {
            existing.setLongitude(newData.getLongitude());
        }
       
    }
    // 상태 관리 getter/setter
    public int getCompletedCount() { return completedCount.get(); }
    public int getFailedCount() { return failedCount.get(); }
    public int getInsertedCount() { return insertedCount.get(); }
    public int getUpdatedCount() { return updatedCount.get(); }
    public int getDeletedCount() { return deletedCount.get(); }

    public void resetCounter() {
        completedCount.set(0);
        failedCount.set(0);
        insertedCount.set(0);
        updatedCount.set(0);
        deletedCount.set(0);
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        resetCounter();
    }
}

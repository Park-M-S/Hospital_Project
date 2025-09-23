package com.hospital.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.HospitalMainApiCaller;
import com.hospital.config.RegionConfig;
import com.hospital.dto.HospitalMainApiItem;
import com.hospital.dto.HospitalMainApiResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.parser.HospitalMainApiParser;
import com.hospital.repository.HospitalMainApiRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HospitalMainAsyncRunner {

    private final RateLimiter rateLimiter = RateLimiter.create(5.0);
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger insertedCount = new AtomicInteger(0);
    private int totalCount = 0;

    private final HospitalMainApiCaller apiCaller;
    private final HospitalMainApiParser parser;
    private final HospitalMainApiRepository hospitalMainApiRepository;
    private final RegionConfig regionConfig;

    private static final int BATCH_SIZE = 100;

    @Autowired
    public HospitalMainAsyncRunner(HospitalMainApiCaller apiCaller,
                                   HospitalMainApiParser parser,
                                   HospitalMainApiRepository hospitalMainApiRepository,
                                   RegionConfig regionConfig) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.hospitalMainApiRepository = hospitalMainApiRepository;
        this.regionConfig = regionConfig;
    }

    @Async("apiExecutor")
    public void runAsync(String sidoCd) {
        rateLimiter.acquire();
        try {
            String sidoName = regionConfig.getSidoName(sidoCd);
            log.info("지역코드 {} 처리 시작", sidoName);


            List<HospitalMain> allHospitals = new ArrayList<>();
            int pageNo = 1;
            int numOfRows = 1000;
            boolean hasMorePages = true;

            while (hasMorePages) {
                String queryParams = String.format("sidoCd=%s&pageNo=%s&numOfRows=%s", sidoCd, pageNo, numOfRows);
                HospitalMainApiResponse response = apiCaller.callApi(queryParams);
                List<HospitalMain> hospitals = parser.parseHospitals(response);

                if (hospitals.isEmpty()) {
                    log.info("지역 {} 페이지 {}: 더 이상 데이터 없음", sidoName, pageNo);
                    break;
                }

                allHospitals.addAll(hospitals);

                // 페이지 단위 대기
                Thread.sleep(1000);
                pageNo++;
                hasMorePages = hospitals.size() >= numOfRows;
            }

            // 배치 저장
            int insertedTotal = 0;
            for (int i = 0; i < allHospitals.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, allHospitals.size());
                List<HospitalMain> batch = allHospitals.subList(i, end);
                hospitalMainApiRepository.saveAll(batch);
                insertedTotal += batch.size();
                log.info("지역 {} 배치 저장: {}건 완료", sidoName, insertedTotal);
            }

            insertedCount.addAndGet(insertedTotal);
            completedCount.incrementAndGet();

            log.info("지역 {} 처리 완료: 총 {}건 저장", sidoName, insertedTotal);

        } catch (Exception e) {
            failedCount.incrementAndGet();
            log.error("지역 코드 {} 처리 실패: {}", regionConfig.getSidoName(sidoCd), e.getMessage());
        }
    }


    // ✅ 상태 관리 메서드
    public int getCompletedCount() {
        return completedCount.get();
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    public int getInsertedCount() {
        return insertedCount.get();
    }

    public void resetCounter() {
        completedCount.set(0);
        failedCount.set(0);
        insertedCount.set(0);
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        resetCounter();
    }
}

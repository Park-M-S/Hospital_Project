package com.hospital.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.ProDocApiCaller;
import com.hospital.config.RegionConfig;
import com.hospital.dto.HospitalMainApiResponse;
import com.hospital.dto.ProDocApiItem;
import com.hospital.dto.ProDocApiResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.entity.ProDoc;
import com.hospital.parser.ProDocApiParser;
import com.hospital.repository.ProDocApiRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProDocAsyncRunner {

    private final RateLimiter rateLimiter = RateLimiter.create(5.0);
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger insertedCount = new AtomicInteger(0);
    private int totalCount = 0;

    private final ProDocApiCaller apiCaller;
    private final ProDocApiParser parser;
    private final ProDocApiRepository proDocApiRepository;
    private final RegionConfig regionConfig;

    private static final int BATCH_SIZE = 100;

    @Autowired
    public ProDocAsyncRunner(ProDocApiCaller apiCaller,
    		ProDocApiParser parser,
    		ProDocApiRepository proDocApiRepository,
    		RegionConfig regionConfig) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.proDocApiRepository = proDocApiRepository;
        this.regionConfig = regionConfig;
    }

    @Async("apiExecutor")
    public void runAsync(String sidoCd) {
        rateLimiter.acquire();
        try {
            String sidoName = regionConfig.getSidoName(sidoCd);
            log.info("지역코드 {} 처리 시작", sidoName);


            List<ProDoc> allHospitals = new ArrayList<>();
            int pageNo = 1;
            int numOfRows = 1000;
            boolean hasMorePages = true;

            while (hasMorePages) {
                String queryParams = String.format("sidoCd=%s&pageNo=%s&numOfRows=%s", sidoCd, pageNo, numOfRows);
                ProDocApiResponse response = apiCaller.callApi(queryParams);
                List<ProDoc> proDocs = parser.parseProDocs(response);

                if (proDocs.isEmpty()) {
                    log.info("지역 {} 페이지 {}: 더 이상 데이터 없음", sidoName, pageNo);
                    break;
                }

                allHospitals.addAll(proDocs);

                Thread.sleep(1000); // 페이지 간 대기
                pageNo++;
                hasMorePages = proDocs.size() >= numOfRows;
            }

            // 배치 저장
            for (int i = 0; i < allHospitals.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, allHospitals.size());
                proDocApiRepository.saveAll(allHospitals.subList(i, end));
            }

            insertedCount.addAndGet(allHospitals.size());
            completedCount.incrementAndGet();
            log.info("지역 {} 처리 완료: 총 {}건 저장", sidoName, allHospitals.size());

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

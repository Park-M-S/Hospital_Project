package com.hospital.async;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Autowired;

import com.hospital.caller.MedicalSubjectApiCaller;

import com.hospital.parser.MedicalSubjectApiParser;

import com.hospital.repository.MedicalSubjectApiRepository;

import com.hospital.dto.MedicalSubjectApiResponse;

import com.hospital.entity.MedicalSubject;
import com.hospital.config.SubjectMappingConfig;

@Service
@Slf4j
public class MedicalSubjectAsyncRunner {

    private final RateLimiter rateLimiter = RateLimiter.create(5.0);
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger insertedCount = new AtomicInteger(0);
    private int totalCount = 0;

    private final MedicalSubjectApiCaller apiCaller;
    private final MedicalSubjectApiParser parser;
    private final MedicalSubjectApiRepository medicalSubjectApiRepository;
    private final SubjectMappingConfig subjectMappingConfig;

    private static final int BATCH_SIZE = 100;

    @Autowired
    public MedicalSubjectAsyncRunner(MedicalSubjectApiCaller apiCaller,
    		MedicalSubjectApiParser parser,
    		MedicalSubjectApiRepository medicalSubjectApiRepository,
                                   SubjectMappingConfig subjectMappingConfig) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.medicalSubjectApiRepository = medicalSubjectApiRepository;
        this.subjectMappingConfig = subjectMappingConfig;
    }

    @Async("apiExecutor")
    public void runAsync(String subjectCode) {
        rateLimiter.acquire();
        try {
            String subjectName = subjectMappingConfig.getDepartmentName(subjectCode);
            log.info("과목코드 {} 처리 시작", subjectName);

            if (subjectCode == null || subjectCode.trim().isEmpty()) {
                throw new IllegalArgumentException("과목코드가 비어있습니다");
            }

           

            int pageNo = 1;
            int numOfRows = 1000;
            boolean hasMorePages = true;

            List<MedicalSubject> batchList = new ArrayList<>();
            int insertedTotal = 0;

            // ✅ 2. API 전체 조회 & 저장
            while (hasMorePages) {
                String queryParams = String.format("dgsbjtCd=%s&pageNo=%s&numOfRows=%s",
                        subjectCode, pageNo, numOfRows);
                MedicalSubjectApiResponse response = apiCaller.callApi(queryParams);

                List<MedicalSubject> subjects = parser.parseSubjects(response, subjectName);

                if (subjects.isEmpty()) {
                    log.info("과목 {} 페이지 {}: 더 이상 데이터 없음", subjectName, pageNo);
                    break;
                }

                batchList.addAll(subjects);

                if (batchList.size() >= BATCH_SIZE) {
                    medicalSubjectApiRepository.saveAll(batchList);
                    insertedTotal += batchList.size();
                    batchList.clear();
                    log.info("과목 {} 배치 저장: 현재까지 {}건 저장", subjectName, insertedTotal);
                }

                hasMorePages = subjects.size() >= numOfRows;
                pageNo++;
                Thread.sleep(1000); // API 호출 간 대기
            }

            // ✅ 3. 마지막 남은 데이터 저장
            if (!batchList.isEmpty()) {
            	medicalSubjectApiRepository.saveAll(batchList);
                insertedTotal += batchList.size();
                log.info("과목 {} 최종 저장: {}건 추가", subjectName, batchList.size());
                batchList.clear();
            }

            // ✅ 4. 카운터 업데이트
            completedCount.incrementAndGet();
            insertedCount.addAndGet(insertedTotal);

            log.info("과목 {} 처리 완료: 총 {}건 저장", subjectName, insertedTotal);

        } catch (Exception e) {
            failedCount.incrementAndGet();
            log.error("과목 코드 {} 처리 실패: {}", subjectMappingConfig.getDepartmentName(subjectCode), e.getMessage(), e);
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

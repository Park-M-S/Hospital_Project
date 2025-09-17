package com.hospital.async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.MedicalSubjectApiCaller;
import com.hospital.dto.MedicalSubjectApiResponse;
import com.hospital.entity.MedicalSubject;
import com.hospital.parser.MedicalSubjectApiParser;
import com.hospital.repository.MedicalSubjectApiRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MedicalSubjectAsyncRunner {
    private final RateLimiter rateLimiter = RateLimiter.create(15.0);
    private final Executor executor;

    private final MedicalSubjectApiCaller apiCaller;
    private final MedicalSubjectApiParser parser;
    private final MedicalSubjectApiRepository repository;

    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger insertedCount = new AtomicInteger(0);
    private final AtomicInteger updatedCount = new AtomicInteger(0);
    private final AtomicInteger deletedCount = new AtomicInteger(0);

    private static final int BATCH_SIZE = 100;
    private static final int CHUNK_SIZE = 200;

    @Autowired
    public MedicalSubjectAsyncRunner(
            MedicalSubjectApiCaller apiCaller, 
            MedicalSubjectApiParser parser,
            MedicalSubjectApiRepository repository,
            @Qualifier("apiExecutor") Executor executor) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.repository = repository;
        this.executor = executor;
    }

    @Async("apiExecutor")
    public void runBatchAsync(List<String> hospitalCodes) {
        log.info("진료과목 멀티스레드 배치 시작: 총 {}건", hospitalCodes.size());

        try {
            // 1. 청크 분할 및 기존 데이터 조회
            List<List<String>> chunks = partitionList(hospitalCodes, CHUNK_SIZE);
            Map<String, MedicalSubject> existingMap = loadExistingSubjects(hospitalCodes);
            log.info("기존 진료과목 데이터 {}건 조회 완료, 총 {}개 청크로 분할", existingMap.size(), chunks.size());

            Set<String> allApiSuccessCodes = ConcurrentHashMap.newKeySet();

            // 2. 청크별 병렬 처리
            List<CompletableFuture<Set<String>>> futures = chunks.stream()
                    .map(chunk -> CompletableFuture.supplyAsync(() -> 
                        processChunk(chunk, existingMap), executor))
                    .collect(Collectors.toList());

            // 3. 모든 청크 완료 대기 및 성공 코드 수집
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    futures.forEach(future -> {
                        try {
                            allApiSuccessCodes.addAll(future.get());
                        } catch (Exception e) {
                            log.error("청크 결과 수집 실패", e);
                        }
                    });

                    // 4. 삭제 처리
                    deleteObsoleteSubjects(allApiSuccessCodes);
                })
                .join();

            log.info("진료과목 배치 완료: 완료 {}, 실패 {}, 신규 {}, 수정 {}, 삭제 {}", 
                    completedCount.get(), failedCount.get(), insertedCount.get(), 
                    updatedCount.get(), deletedCount.get());

        } catch (Exception e) {
            failedCount.addAndGet(hospitalCodes.size());
            log.error("전체 배치 실패", e);
        }
    }

    private Set<String> processChunk(List<String> chunk, Map<String, MedicalSubject> existingMap) {
        String threadName = Thread.currentThread().getName();
        log.debug("[{}] 청크 처리 시작: {}건", threadName, chunk.size());

        List<MedicalSubject> toInsert = new ArrayList<>();
        List<MedicalSubject> toUpdate = new ArrayList<>();
        Set<String> chunkSuccessCodes = new HashSet<>();

        for (String hospitalCode : chunk) {
            try {
                rateLimiter.acquire();

                String queryParams = "ykiho=" + hospitalCode;
                MedicalSubjectApiResponse response = apiCaller.callApi(queryParams);

                // 파서 호출
                List<MedicalSubject> parsedList = parser.parse(response, hospitalCode);

                if (!parsedList.isEmpty()) {
                    MedicalSubject newRecord = parsedList.get(0); // 병원당 하나만 반환
                    chunkSuccessCodes.add(hospitalCode);

                    MedicalSubject existing = existingMap.get(hospitalCode);
                    if (existing != null) {
                        if (updateFields(existing, newRecord)) {
                            toUpdate.add(existing);
                        }
                    } else {
                        toInsert.add(newRecord);
                    }
                }
                completedCount.incrementAndGet();

                // 배치 저장
                if (toInsert.size() + toUpdate.size() >= BATCH_SIZE) {
                    saveBatchAndClear(toInsert, toUpdate);
                }

            } catch (Exception e) {
                failedCount.incrementAndGet();
                log.error("[{}] API 호출 실패: {}", threadName, hospitalCode, e);
            }
        }

        if (!toInsert.isEmpty() || !toUpdate.isEmpty()) {
            saveBatchAndClear(toInsert, toUpdate);
        }

        log.debug("[{}] 청크 처리 완료: 성공 {}건", threadName, chunkSuccessCodes.size());
        return chunkSuccessCodes;
    }

    private void deleteObsoleteSubjects(Set<String> apiSuccessCodes) {
        List<String> existingCodes = repository.findAllDistinctHospitalCodes();
        List<String> toDelete = existingCodes.stream()
                .filter(code -> !apiSuccessCodes.contains(code))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            repository.deleteByHospitalCodeIn(toDelete);
            deletedCount.addAndGet(toDelete.size());
            log.info("폐업/제외된 병원 진료과목 삭제: {}건", toDelete.size());
        }
    }

    private synchronized void saveBatchAndClear(List<MedicalSubject> toInsert, List<MedicalSubject> toUpdate) {
        if (!toInsert.isEmpty()) {
            repository.saveAll(toInsert);
            insertedCount.addAndGet(toInsert.size());
            toInsert.clear();
        }

        if (!toUpdate.isEmpty()) {
            repository.saveAll(toUpdate);
            updatedCount.addAndGet(toUpdate.size());
            toUpdate.clear();
        }
    }

    private Map<String, MedicalSubject> loadExistingSubjects(List<String> hospitalCodes) {
        List<MedicalSubject> existingList = repository.findByHospitalCodeIn(hospitalCodes);
        return existingList.stream()
                .collect(Collectors.toMap(
                    MedicalSubject::getHospitalCode,
                    Function.identity(),
                    (existing, duplicate) -> {
                        log.warn("중복된 병원코드 발견: {} - 기존 레코드 유지 (ID: {})", 
                                existing.getHospitalCode(), existing.getId());
                        return existing;
                    }
                ));
    }

    private List<List<String>> partitionList(List<String> list, int size) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }

    private boolean updateFields(MedicalSubject existing, MedicalSubject newData) {
        boolean hasChanges = false;

        if (!Objects.equals(existing.getSubjectName(), newData.getSubjectName())) {
            existing.setSubjectName(newData.getSubjectName());
            hasChanges = true;
        }// setSubjects -> getSubjectName

        return hasChanges;
    }

    // 상태 관리
    public void resetCounter() {
        completedCount.set(0);
        failedCount.set(0);
        insertedCount.set(0);
        updatedCount.set(0);
        deletedCount.set(0);
    }

    public int getCompletedCount() { return completedCount.get(); }
    public int getFailedCount() { return failedCount.get(); }
    public int getInsertedCount() { return insertedCount.get(); }
    public int getUpdatedCount() { return updatedCount.get(); }
    public int getDeletedCount() { return deletedCount.get(); }
}

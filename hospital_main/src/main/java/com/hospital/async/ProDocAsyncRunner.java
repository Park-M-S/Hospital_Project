package com.hospital.async;

import java.util.ArrayList;
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
import com.hospital.caller.ProDocApiCaller;
import com.hospital.dto.ProDocApiResponse;
import com.hospital.entity.ProDoc;
import com.hospital.parser.ProDocApiParser;
import com.hospital.repository.ProDocApiRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProDocAsyncRunner {
    private final RateLimiter rateLimiter = RateLimiter.create(10.0);
    private final Executor executor;

    private final ProDocApiCaller apiCaller;
    private final ProDocApiParser parser;
    private final ProDocApiRepository repository;

    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger insertedCount = new AtomicInteger(0);
    private final AtomicInteger updatedCount = new AtomicInteger(0);
    private final AtomicInteger deletedCount = new AtomicInteger(0);

    private static final int BATCH_SIZE = 100;
    private static final int CHUNK_SIZE = 100;

    @Autowired
    public ProDocAsyncRunner(ProDocApiCaller apiCaller, ProDocApiParser parser,
            ProDocApiRepository repository, @Qualifier("apiExecutor") Executor executor) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.repository = repository;
        this.executor = executor;
    }

    @Async("apiExecutor")
    public void runBatchAsync(List<String> hospitalCodes) {
        log.info("전문의 정보 멀티스레드 배치 시작: 총 {}건", hospitalCodes.size());

        try {
            // 1. 청크 분할 및 기존 데이터 조회
            List<List<String>> chunks = partitionList(hospitalCodes, CHUNK_SIZE);
            Map<String, ProDoc> existingMap = loadExistingProDocs(hospitalCodes);
            log.info("기존 전문의 데이터 {}건 조회 완료, 총 {}개 청크로 분할", existingMap.size(), chunks.size());

            // 2. API 성공 코드 추적 (thread-safe)
            Set<String> allApiSuccessCodes = ConcurrentHashMap.newKeySet();

            // 3. 청크별 병렬 처리
            List<CompletableFuture<Set<String>>> futures = chunks.stream()
                    .map(chunk -> CompletableFuture.supplyAsync(() -> 
                        processChunk(chunk, existingMap), executor))
                    .collect(Collectors.toList());

            // 4. 모든 청크 완료 대기 및 성공 코드 수집
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    futures.forEach(future -> {
                        try {
                            allApiSuccessCodes.addAll(future.get());
                        } catch (Exception e) {
                            log.error("청크 결과 수집 실패", e);
                        }
                    });

                    // 5. 삭제 처리
                    deleteObsoleteProDocs(allApiSuccessCodes);
                })
                .join();

            log.info("전문의 정보 배치 완료: 완료 {}, 실패 {}, 신규 {}, 수정 {}, 삭제 {}", 
                    completedCount.get(), failedCount.get(), insertedCount.get(), 
                    updatedCount.get(), deletedCount.get());

        } catch (Exception e) {
            failedCount.addAndGet(hospitalCodes.size());
            log.error("전체 배치 실패", e);
        }
    }

    private Set<String> processChunk(List<String> chunk, Map<String, ProDoc> existingMap) {
        String threadName = Thread.currentThread().getName();
        log.debug("[{}] 청크 처리 시작: {}건", threadName, chunk.size());

        List<ProDoc> toInsert = new ArrayList<>();
        List<ProDoc> toUpdate = new ArrayList<>();
        Set<String> chunkSuccessCodes = new HashSet<>();

        for (String hospitalCode : chunk) {
            try {
                rateLimiter.acquire();

                String queryParams = "ykiho=" + hospitalCode;
                ProDocApiResponse response = apiCaller.callApi(queryParams);
                
                // ✅ 새로운 파서 메서드 사용 (병원당 하나의 레코드)
                List<ProDoc> parsedList = parser.parse(response, hospitalCode);

                if (!parsedList.isEmpty()) {
                    ProDoc newProDoc = parsedList.get(0); // 항상 하나의 레코드만 반환됨
                    chunkSuccessCodes.add(hospitalCode);

                    ProDoc existing = existingMap.get(hospitalCode);
                    if (existing != null) {
                        if (updateProDocFields(existing, newProDoc)) {
                            toUpdate.add(existing);
                        }
                    } else {
                        toInsert.add(newProDoc);
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

        // 청크 완료 후 남은 데이터 저장
        if (!toInsert.isEmpty() || !toUpdate.isEmpty()) {
            saveBatchAndClear(toInsert, toUpdate);
        }

        log.debug("[{}] 청크 처리 완료: 성공 {}건", threadName, chunkSuccessCodes.size());
        return chunkSuccessCodes;
    }

    private void deleteObsoleteProDocs(Set<String> apiSuccessCodes) {
    	// 기존 ProDoc에 있던 병원코드만 가져와서
    	List<String> existingCodes = repository.findAllDistinctHospitalCodes();

    	// 기존에는 있었는데 이번 API에서 응답이 없는 것만 삭제
    	List<String> toDelete = existingCodes.stream()
    	    .filter(code -> !apiSuccessCodes.contains(code))
    	    .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            repository.deleteByHospitalCodeIn(toDelete);
            deletedCount.addAndGet(toDelete.size());
            log.info("폐업/제외된 병원 전문의 정보 삭제: {}건", toDelete.size());
        }
    }

    private synchronized void saveBatchAndClear(List<ProDoc> toInsert, List<ProDoc> toUpdate) {
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

    private Map<String, ProDoc> loadExistingProDocs(List<String> hospitalCodes) {
        List<ProDoc> existingProDocs = repository.findByHospitalCodeIn(hospitalCodes);
        return existingProDocs.stream()
                .collect(Collectors.toMap(
                    ProDoc::getHospitalCode, 
                    Function.identity(),
                    (existing, duplicate) -> {
                        log.warn("중복된 병원코드 발견: {} - 기존 레코드 유지 (ID: {})", 
                                existing.getHospitalCode(), existing.getId());
                        return existing; // 첫 번째 레코드 유지
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

    /**
     * ✅ 새로운 구조에 맞게 필드 업데이트 (실제 변경이 있을 때만 true 반환)
     */
    private boolean updateProDocFields(ProDoc existing, ProDoc newData) {
        boolean hasChanges = false;

        // subjectDetails 필드만 비교
        if (!Objects.equals(existing.getProDocList(), newData.getProDocList())) {
            existing.setProDocList(newData.getProDocList());
            hasChanges = true; // setSubjectDetails-> getSubjectName
        }

        return hasChanges;
    }

    // 상태 관리 메서드들
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
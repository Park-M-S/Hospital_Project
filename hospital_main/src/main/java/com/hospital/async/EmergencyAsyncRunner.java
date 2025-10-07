package com.hospital.async;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.caller.EmergencyApiCaller;
import com.hospital.config.RegionConfig;
import com.hospital.dto.EmergencyWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Service
@Slf4j
public class EmergencyAsyncRunner {

    private final EmergencyApiCaller apiCaller;
    private final RegionConfig regionConfig;
    private final ObjectMapper objectMapper;
    private final TaskScheduler taskScheduler;
    
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledTask;
    
    // 통계
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger processedCount = new AtomicInteger(0);

    @Autowired
    public EmergencyAsyncRunner(EmergencyApiCaller apiCaller, 
                               RegionConfig regionConfig,
                               TaskScheduler taskScheduler) {
        this.apiCaller = apiCaller;
        this.regionConfig = regionConfig;
        this.taskScheduler = taskScheduler;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 30초마다 반복 실행하는 스케줄러 시작
     */
    public void runAsyncForAllCities(Consumer<List<EmergencyWebResponse>> callback) {
        if (running.compareAndSet(false, true)) {
            log.info("✅ 응급실 30초 주기 스케줄러 시작");
            
            // 즉시 첫 실행
            taskScheduler.schedule(() -> collectAllCitiesData(callback), Instant.now());
            
            // 30초마다 반복 실행
            scheduledTask = taskScheduler.scheduleWithFixedDelay(() -> {
                if (running.get()) {
                    try {
                        collectAllCitiesData(callback);
                    } catch (Exception e) {
                        log.error("스케줄 실행 중 오류 발생: {}", e.getMessage(), e);
                    }
                }
            }, Instant.now().plusSeconds(30), Duration.ofSeconds(30));
        } else {
            log.warn("이미 스케줄러가 실행 중입니다.");
        }
    }

    /**
     * 모든 도시 데이터를 순차적으로 수집 (동시 호출 방지)
     */
    private void collectAllCitiesData(Consumer<List<EmergencyWebResponse>> callback) {
        long startTime = System.currentTimeMillis();
        log.info("🔄 응급실 데이터 수집 시작 - 도시 수: {}", regionConfig.getEmergencyCityNames().size());
        
        List<EmergencyWebResponse> allCityData = new ArrayList<>();
        resetCounters();
        
        // 순차 처리로 API 부하 방지
        for (String city : regionConfig.getEmergencyCityNames()) {
            if (!running.get()) {
                log.info("⏹️ 수집 중단됨");
                return;
            }
            
            try {
                List<EmergencyWebResponse> cityData = collectCityData(city);
                if (!cityData.isEmpty()) {
                    allCityData.addAll(cityData);
                    processedCount.addAndGet(cityData.size());
                    log.debug("✅ {} 수집 완료 - {} 건", city, cityData.size());
                } else {
                    log.debug("⚠️ {} 데이터 없음", city);
                }
                completedCount.incrementAndGet();
                
                // 도시 간 딜레이 (API 부하 방지)
                if (!city.equals(regionConfig.getEmergencyCityNames().get(regionConfig.getEmergencyCityNames().size() - 1))) {
                    Thread.sleep(10000); // 마지막 도시가 아니면 대기
                }
                
            } catch (Exception e) {
                failedCount.incrementAndGet();
                log.error("❌ {} 수집 실패: {}", city, e.getMessage());
            }
        }
        
        // 수집된 데이터가 있으면 콜백 호출
        if (!allCityData.isEmpty()) {
            callback.accept(allCityData);
            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ 전체 수집 완료 - 총 {} 건 (성공: {}, 실패: {}, 소요시간: {}ms)", 
                processedCount.get(), completedCount.get(), failedCount.get(), duration);
        } else {
            log.warn("⚠️ 수집된 데이터가 없습니다.");
        }
    }

    /**
     * 특정 도시의 응급실 데이터 수집 (페이지네이션 포함)
     */
    private List<EmergencyWebResponse> collectCityData(String city) throws Exception {
        List<EmergencyWebResponse> cityData = new ArrayList<>();
        int pageNo = 1;
        int maxPages = 3; // API 부하를 고려해 페이지 수 제한
        int consecutiveEmptyPages = 0;
        
        while (pageNo <= maxPages && running.get() && consecutiveEmptyPages < 2) {
            try {
                List<JsonNode> responseList = apiCaller.callEmergencyApiByCityPage(city, pageNo, 10);
                
                if (responseList == null || responseList.isEmpty()) {
                    consecutiveEmptyPages++;
                    log.debug("📭 {} 페이지 {} - 응답 없음 (연속 빈 페이지: {})", city, pageNo, consecutiveEmptyPages);
                    pageNo++;
                    continue;
                }
                
                List<EmergencyWebResponse> pageData = new ArrayList<>();
                for (JsonNode node : responseList) {
                    try {
                        JsonNode bodyNode = node.path("body");
                        if (bodyNode.isMissingNode()) continue;
                        
                        JsonNode itemsNode = bodyNode.path("items");
                        if (itemsNode.isMissingNode()) continue;
                        
                        JsonNode itemNode = itemsNode.path("item");
                        if (itemNode.isMissingNode() || !itemNode.isArray()) continue;
                        
                        EmergencyWebResponse[] arr = objectMapper.treeToValue(itemNode, EmergencyWebResponse[].class);
                        if (arr != null && arr.length > 0) {
                            pageData.addAll(Arrays.asList(arr));
                        }
                    } catch (Exception parseEx) {
                        log.warn("⚠️ {} 페이지 {} JSON 파싱 오류: {}", city, pageNo, parseEx.getMessage());
                    }
                }
                
                if (pageData.isEmpty()) {
                    consecutiveEmptyPages++;
                    log.debug("📭 {} 페이지 {} - 파싱된 데이터 없음", city, pageNo);
                } else {
                    consecutiveEmptyPages = 0; // 데이터가 있으면 카운터 리셋
                    cityData.addAll(pageData);
                    log.debug("📄 {} 페이지 {} 수집 완료 - {} 건", city, pageNo, pageData.size());
                    
                    // 페이지 데이터가 100건 미만이면 마지막 페이지로 간주
                    if (pageData.size() < 100) {
                        log.debug("📄 {} 마지막 페이지 도달 (데이터: {} < 100)", city, pageData.size());
                        break;
                    }
                }
                
                pageNo++;
                
                // 페이지 간 짧은 딜레이 (API 부하 방지)
                if (pageNo <= maxPages) {
                    Thread.sleep(300);
                }
                
            } catch (Exception e) {
                log.error("❌ {} 페이지 {} 오류: {}", city, pageNo, e.getMessage());
                // 첫 페이지에서 실패하면 도시 전체 실패로 처리
                if (pageNo == 1) {
                    throw new Exception("첫 페이지 호출 실패: " + e.getMessage(), e);
                }
                // 중간 페이지 실패는 여기서 종료
                break;
            }
        }
        
        return cityData;
    }

    /**
     * 스케줄러 중지
     */
    public void stopAsync() {
        if (running.compareAndSet(true, false)) {
            log.info("🔄 응급실 스케줄러 중지 요청");
            
            if (scheduledTask != null && !scheduledTask.isDone()) {
                boolean cancelled = scheduledTask.cancel(false);
                log.info("📋 스케줄 태스크 취소 결과: {}", cancelled);
            }
            
            log.info("✅ 응급실 스케줄러 중지 완료");
        } else {
            log.debug("⚠️ 스케줄러가 이미 중지되어 있습니다.");
        }
    }

    // 상태 조회 메서드들
    public boolean isRunning() { 
        return running.get() && scheduledTask != null && !scheduledTask.isDone(); 
    }
    
    public int getCompletedCount() { return completedCount.get(); }
    public int getFailedCount() { return failedCount.get(); }
    public int getProcessedCount() { return processedCount.get(); }
    
    private void resetCounters() {
        completedCount.set(0);
        failedCount.set(0);
        processedCount.set(0);
    }
    
    /**
     * 통계 정보 반환
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("running", isRunning());
        stats.put("completed", completedCount.get());
        stats.put("failed", failedCount.get());
        stats.put("processed", processedCount.get());
        stats.put("totalCities", regionConfig.getEmergencyCityNames().size());
        return stats;
    }
}
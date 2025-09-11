package com.hospital.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

/**
 * 커스텀 메트릭 관리 클래스
 */
@Component
public class CustomMetrics {

    private final MeterRegistry meterRegistry;
    
    // 기본 API 메트릭들
    private final Timer apiResponseTime;
    private final Counter apiRequestCount;
    private final Counter apiErrorCount;
    private final Counter apiSuccessCount;
    
    // 실시간 상태 메트릭을 위한 AtomicLong
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong queueSize = new AtomicLong(0);

    @Autowired
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 기본 API 메트릭 초기화
        this.apiResponseTime = Timer.builder("api.response.time")
                .description("API response time in seconds")
                .register(meterRegistry);
                
        this.apiRequestCount = Counter.builder("api.requests.total")
                .description("Total number of API requests")
                .register(meterRegistry);
                
        this.apiErrorCount = Counter.builder("api.errors.total")
                .description("Total number of API errors")
                .register(meterRegistry);
                
        this.apiSuccessCount = Counter.builder("api.success.total")
                .description("Total number of successful API requests")
                .register(meterRegistry);
    }

    @PostConstruct
    public void initGauges() {
        // 실시간 상태 게이지 등록
        Gauge.builder("app.users.active")
                .description("Number of currently active users")
                .register(meterRegistry, activeUsers, AtomicLong::doubleValue);
                
        Gauge.builder("app.connections.active")
                .description("Number of currently active database connections")
                .register(meterRegistry, activeConnections, AtomicLong::doubleValue);
                
        Gauge.builder("app.queue.size")
                .description("Current queue size for processing")
                .register(meterRegistry, queueSize, AtomicLong::doubleValue);
    }

    // === 기본 API 메트릭 메소드들 ===
    
    /**
     * 타이머 샘플 시작
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * API 응답 시간 기록
     */
    public void recordResponseTime(Timer.Sample sample, String endpoint, String method) {
        sample.stop(Timer.builder("api.response.time")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry));
    }

    /**
     * API 요청 수 증가
     */
    public void incrementRequestCount(String endpoint, String method) {
        Counter.builder("api.requests.total")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry)
                .increment();
    }

    /**
     * API 에러 수 증가
     */
    public void incrementErrorCount(String endpoint, String method, String errorType) {
        Counter.builder("api.errors.total")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * API 성공 수 증가
     */
    public void incrementSuccessCount(String endpoint, String method, int statusCode) {
        Counter.builder("api.success.total")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry)
                .increment();
    }

    // === 실시간 상태 메트릭 메소드들 ===
    
    /**
     * 활성 사용자 수 설정
     */
    public void setActiveUsers(long count) {
        activeUsers.set(count);
    }

    /**
     * 활성 사용자 수 증가
     */
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    /**
     * 활성 사용자 수 감소
     */
    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }

    /**
     * 활성 DB 연결 수 설정
     */
    public void setActiveConnections(long count) {
        activeConnections.set(count);
    }

    /**
     * 큐 사이즈 설정
     */
    public void setQueueSize(long size) {
        queueSize.set(size);
    }

    // === 커스텀 메트릭 기록 메소드들 ===
    
    /**
     * 커스텀 카운터 증가
     */
    public void incrementCustomCounter(String name, String... tags) {
        Counter.Builder builder = Counter.builder(name);
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                builder.tag(tags[i], tags[i + 1]);
            }
        }
        builder.register(meterRegistry).increment();
    }

    /**
     * 커스텀 게이지 기록
     */
    public void recordCustomGauge(String name, double value, String... tags) {
        Tags tagList = Tags.empty();
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                tagList = tagList.and(tags[i], tags[i + 1]);
            }
        }
        
        Gauge.builder(name)
                .description("Custom gauge metric")
                .tags(tagList)
                .register(meterRegistry, () -> value);
    }

    /**
     * 커스텀 타이머 기록
     */
    public void recordCustomTimer(String name, long duration, String... tags) {
        Timer.Builder builder = Timer.builder(name);
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                builder.tag(tags[i], tags[i + 1]);
            }
        }
        builder.register(meterRegistry).record(duration, TimeUnit.MILLISECONDS);
    }

    // === 통계 조회 메소드들 ===
    
    /**
     * 현재 활성 사용자 수 조회
     */
    public long getCurrentActiveUsers() {
        return activeUsers.get();
    }

    /**
     * 현재 활성 연결 수 조회
     */
    public long getCurrentActiveConnections() {
        return activeConnections.get();
    }

    /**
     * 현재 큐 사이즈 조회
     */
    public long getCurrentQueueSize() {
        return queueSize.get();
    }
}

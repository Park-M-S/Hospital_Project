package com.hospital.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

/**
 * 안전한 커스텀 메트릭 관리 클래스
 */
@Component
public class CustomMetrics {

    private final MeterRegistry meterRegistry;
    
    // 실시간 상태 메트릭
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong queueSize = new AtomicLong(0);

    @Autowired
    public CustomMetrics(@Qualifier("prometheusMeterRegistry") MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void initGauges() {
        try {
            // 가장 안전한 Gauge 등록 방식
            meterRegistry.gauge("app.users.active", activeUsers, AtomicLong::doubleValue);
            meterRegistry.gauge("app.connections.active", activeConnections, AtomicLong::doubleValue);
            meterRegistry.gauge("app.queue.size", queueSize, AtomicLong::doubleValue);
        } catch (Exception e) {
            System.err.println("Gauge initialization failed: " + e.getMessage());
        }
    }

    // === 기본 API 메트릭 메소드들 ===
    
    /**
     * 타이머 샘플 시작
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * API 응답 시간 기록 - 안전한 방식
     */
    public void recordResponseTime(Timer.Sample sample, String endpoint, String method) {
        try {
            sample.stop(Timer.builder("api.response.time")
                    .tag("endpoint", sanitizeTag(endpoint))
                    .tag("method", sanitizeTag(method))
                    .register(meterRegistry));
        } catch (Exception e) {
            // 메트릭 수집 실패가 애플리케이션에 영향주지 않도록
            System.err.println("Metrics recording failed: " + e.getMessage());
        }
    }

    /**
     * API 요청 수 증가
     */
    public void incrementRequestCount(String endpoint, String method) {
        try {
            Counter.builder("api.requests.total")
                    .tag("endpoint", sanitizeTag(endpoint))
                    .tag("method", sanitizeTag(method))
                    .register(meterRegistry)
                    .increment();
        } catch (Exception e) {
            System.err.println("Request count increment failed: " + e.getMessage());
        }
    }

    /**
     * API 에러 수 증가
     */
    public void incrementErrorCount(String endpoint, String method, String errorType) {
        try {
            Counter.builder("api.errors.total")
                    .tag("endpoint", sanitizeTag(endpoint))
                    .tag("method", sanitizeTag(method))
                    .tag("error_type", sanitizeTag(errorType))
                    .register(meterRegistry)
                    .increment();
        } catch (Exception e) {
            System.err.println("Error count increment failed: " + e.getMessage());
        }
    }

    /**
     * API 성공 수 증가
     */
    public void incrementSuccessCount(String endpoint, String method, int statusCode) {
        try {
            Counter.builder("api.success.total")
                    .tag("endpoint", sanitizeTag(endpoint))
                    .tag("method", sanitizeTag(method))
                    .tag("status", String.valueOf(statusCode))
                    .register(meterRegistry)
                    .increment();
        } catch (Exception e) {
            System.err.println("Success count increment failed: " + e.getMessage());
        }
    }

    // === 실시간 상태 메트릭 메소드들 ===
    
    public void setActiveUsers(long count) {
        activeUsers.set(Math.max(0, count));
    }

    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    public void decrementActiveUsers() {
        long current = activeUsers.get();
        if (current > 0) {
            activeUsers.decrementAndGet();
        }
    }

    public void setActiveConnections(long count) {
        activeConnections.set(Math.max(0, count));
    }

    public void setQueueSize(long size) {
        queueSize.set(Math.max(0, size));
    }

    // === 커스텀 메트릭 기록 메소드들 ===
    
    /**
     * 안전한 커스텀 카운터 증가
     */
    public void incrementCustomCounter(String name, String... tags) {
        try {
            Counter.Builder builder = Counter.builder(sanitizeMetricName(name));
            for (int i = 0; i < tags.length; i += 2) {
                if (i + 1 < tags.length) {
                    builder.tag(sanitizeTag(tags[i]), sanitizeTag(tags[i + 1]));
                }
            }
            builder.register(meterRegistry).increment();
        } catch (Exception e) {
            System.err.println("Custom counter increment failed: " + e.getMessage());
        }
    }

    /**
     * 안전한 커스텀 타이머 기록
     */
    public void recordCustomTimer(String name, long duration, String... tags) {
        try {
            Timer.Builder builder = Timer.builder(sanitizeMetricName(name));
            for (int i = 0; i < tags.length; i += 2) {
                if (i + 1 < tags.length) {
                    builder.tag(sanitizeTag(tags[i]), sanitizeTag(tags[i + 1]));
                }
            }
            builder.register(meterRegistry).record(Math.max(0, duration), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.err.println("Custom timer recording failed: " + e.getMessage());
        }
    }

    // === 통계 조회 메소드들 ===
    
    public long getCurrentActiveUsers() {
        return activeUsers.get();
    }

    public long getCurrentActiveConnections() {
        return activeConnections.get();
    }

    public long getCurrentQueueSize() {
        return queueSize.get();
    }

    // === 안전성을 위한 헬퍼 메소드들 ===
    
    /**
     * 메트릭 이름 정제 - 안전한 문자만 사용
     */
    private String sanitizeMetricName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "unknown.metric";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }

    /**
     * 태그 값 정제 - 카디널리티 폭발 방지
     */
    private String sanitizeTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return "unknown";
        }
        
        String sanitized = tag.trim();
        
        // 길이 제한 (메모리 보호)
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        // 특수 문자 제거
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._/-]", "_");
        
        return sanitized.toLowerCase();
    }
}

package com.hospital.controller;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Spring Framework용 모니터링 엔드포인트
 * Spring Boot Actuator 대신 사용
 */
@RestController
@RequestMapping("/actuator")
public class MonitoringController {

    @Autowired
    private PrometheusMeterRegistry prometheusMeterRegistry;

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * 프로메테우스 메트릭 엔드포인트
     * GET /actuator/prometheus
     */
    @GetMapping(value = "/prometheus", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> prometheus() {
        try {
            String metrics = prometheusMeterRegistry.scrape();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("# Error collecting metrics: " + e.getMessage());
        }
    }

    /**
     * 헬스체크 엔드포인트
     * GET /actuator/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        boolean isHealthy = true;
        
        try {
            // 애플리케이션 기본 상태
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            
            // 컴포넌트별 상태 확인
            Map<String, Object> components = new LinkedHashMap<>();
            
            // 데이터베이스 연결 체크
            Map<String, Object> dbHealth = checkDatabaseHealth();
            components.put("database", dbHealth);
            if ("DOWN".equals(dbHealth.get("status"))) {
                isHealthy = false;
            }
            
            // 메모리 상태 체크
            Map<String, Object> memoryHealth = checkMemoryHealth();
            components.put("memory", memoryHealth);
            
            // 디스크 공간 체크
            Map<String, Object> diskHealth = checkDiskHealth();
            components.put("diskSpace", diskHealth);
            if ("DOWN".equals(diskHealth.get("status"))) {
                isHealthy = false;
            }
            
            health.put("components", components);
            
            // 전체 상태 결정
            if (!isHealthy) {
                health.put("status", "DOWN");
                return ResponseEntity.status(503).body(health);
            }
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
        
        return ResponseEntity.ok(health);
    }

    /**
     * 애플리케이션 정보 엔드포인트
     * GET /actuator/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        
        // 애플리케이션 정보
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("name", "Hospital Management System");
        app.put("version", "1.0.0");
        app.put("description", "Hospital Management Backend API");
        info.put("app", app);
        
        // 시스템 정보
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("framework", "Spring Framework 6.0.13");
        system.put("java_version", System.getProperty("java.version"));
        system.put("java_vendor", System.getProperty("java.vendor"));
        system.put("os_name", System.getProperty("os.name"));
        system.put("os_version", System.getProperty("os.version"));
        info.put("system", system);
        
        // 빌드 정보 (환경변수에서 가져오기)
        Map<String, Object> build = new LinkedHashMap<>();
        build.put("time", System.getProperty("build.time", "unknown"));
        build.put("version", System.getProperty("build.version", "1.0.0"));
        info.put("build", build);
        
        return ResponseEntity.ok(info);
    }

    /**
     * 메트릭 엔드포인트 (간단한 JSON 형태)
     * GET /actuator/metrics
     */
    @GetMapping(value = "/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        
        // JVM 메트릭
        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("memory_used", runtime.totalMemory() - runtime.freeMemory());
        jvm.put("memory_total", runtime.totalMemory());
        jvm.put("memory_max", runtime.maxMemory());
        jvm.put("memory_usage_percent", 
            Math.round(((double)(runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory()) * 100));
        metrics.put("jvm", jvm);
        
        // 시스템 메트릭
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("processors", Runtime.getRuntime().availableProcessors());
        system.put("uptime", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime());
        metrics.put("system", system);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * 데이터베이스 헬스체크
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new LinkedHashMap<>();
        
        if (dataSource == null) {
            dbHealth.put("status", "UNKNOWN");
            dbHealth.put("details", "DataSource not configured");
            return dbHealth;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            dbHealth.put("status", "UP");
            dbHealth.put("database", conn.getMetaData().getDatabaseProductName());
            dbHealth.put("version", conn.getMetaData().getDatabaseProductVersion());
            dbHealth.put("url", conn.getMetaData().getURL());
            dbHealth.put("validation_query", "SELECT 1");
        } catch (SQLException e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
            dbHealth.put("error_code", e.getErrorCode());
        }
        
        return dbHealth;
    }

    /**
     * 메모리 헬스체크
     */
    private Map<String, Object> checkMemoryHealth() {
        Map<String, Object> memoryHealth = new LinkedHashMap<>();
        Runtime runtime = Runtime.getRuntime();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usagePercent = (double) usedMemory / maxMemory * 100;
        
        memoryHealth.put("status", usagePercent > 90 ? "WARNING" : "UP");
        memoryHealth.put("used", usedMemory);
        memoryHealth.put("total", totalMemory);
        memoryHealth.put("max", maxMemory);
        memoryHealth.put("usage_percent", Math.round(usagePercent));
        
        if (usagePercent > 90) {
            memoryHealth.put("warning", "Memory usage is above 90%");
        }
        
        return memoryHealth;
    }

    /**
     * 디스크 공간 헬스체크
     */
    private Map<String, Object> checkDiskHealth() {
        Map<String, Object> diskHealth = new LinkedHashMap<>();
        
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            double usagePercent = (double) usedSpace / totalSpace * 100;
            
            diskHealth.put("status", usagePercent > 95 ? "DOWN" : usagePercent > 85 ? "WARNING" : "UP");
            diskHealth.put("total", totalSpace);
            diskHealth.put("free", freeSpace);
            diskHealth.put("used", usedSpace);
            diskHealth.put("usage_percent", Math.round(usagePercent));
            
            if (usagePercent > 95) {
                diskHealth.put("error", "Disk usage is critically high (>95%)");
            } else if (usagePercent > 85) {
                diskHealth.put("warning", "Disk usage is high (>85%)");
            }
            
        } catch (Exception e) {
            diskHealth.put("status", "UNKNOWN");
            diskHealth.put("error", e.getMessage());
        }
        
        return diskHealth;
    }
}

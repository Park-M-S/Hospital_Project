package com.hospital.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Spring Framework용 Micrometer 메트릭 설정
 */
@Configuration
public class MetricsConfig {

    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        
        // JVM 메트릭 등록
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        
        // 시스템 메트릭 등록
        new ProcessorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);
        new FileDescriptorMetrics().bindTo(registry);
        
        // 디스크 공간 메트릭 (루트 디렉토리)
        new DiskSpaceMetrics(new File("/")).bindTo(registry);
        
        // 애플리케이션 정보 메트릭
        Gauge.builder("hospital.application.info")
            .description("Application information")
            .tag("version", "1.0.0")
            .tag("framework", "Spring Framework 6.0.13")
            .tag("java_version", System.getProperty("java.version"))
            .register(registry, 1.0);
        
        return registry;
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return prometheusMeterRegistry();
    }
}

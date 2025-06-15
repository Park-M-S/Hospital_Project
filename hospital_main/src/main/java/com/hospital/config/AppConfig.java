package com.hospital.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Hospital API 메인 설정 클래스
 * - 기본 컴포넌트 스캔 및 활성화 설정
 * - 다른 설정 클래스들을 Import로 조합
 */
@Configuration
@ComponentScan(basePackages = "com.hospital")
@PropertySource("classpath:api.properties")
@PropertySource("classpath:db.properties")
@PropertySource("classpath:region.properties")
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.hospital.repository")
@EnableWebMvc
@EnableScheduling
@EnableAsync
@EnableAspectJAutoProxy
@Import({
    DatabaseConfig.class,      // 데이터베이스 설정
    WebConfig.class,          // 웹 및 HTTP 설정
    AsyncConfig.class,        // 비동기 및 스케줄링 설정
    WebSocketConfig.class,    // WebSocket 설정
    JacksonConfig.class, 
    CacheConfig.class
})
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
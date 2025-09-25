package com.hospital.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 데이터베이스 관련 설정 (커넥션 풀 적용)
 * - HikariCP 커넥션 풀 설정
 * - JPA EntityManager 설정
 * - 트랜잭션 매니저 설정
 * - 대량 데이터 처리 최적화
 */
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource(@Value("${jdbc.driverClassName}") String driverClassName,
                                 @Value("${jdbc.url}") String url,
                                 @Value("${jdbc.username}") String username,
                                 @Value("${jdbc.password}") String password) {
        
        // 🔹 HikariCP 설정 (고성능 커넥션 풀)
        HikariConfig config = new HikariConfig();
        
        // 기본 연결 정보
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        
        // 🔹 커넥션 풀 설정 (8만개 처리용)
        config.setMaximumPoolSize(50);          // 최대 50개 연결
        config.setMinimumIdle(20);              // 최소 20개 유지
        config.setConnectionTimeout(30000);     // 연결 타임아웃 30초
        config.setIdleTimeout(600000);          // 유휴 타임아웃 10분
        config.setMaxLifetime(1800000);         // 최대 생존 시간 30분
        config.setLeakDetectionThreshold(60000); // 커넥션 리크 감지 60초
        
        // 🔹 성능 최적화 설정
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true"); // 배치 INSERT 최적화 ⭐
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // MariaDB/MySQL 특화 설정
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8mb4");
        config.addDataSourceProperty("serverTimezone", "Asia/Seoul");
        
        // 커넥션 풀 이름 설정
        config.setPoolName("HospitalDB-HikariCP");
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        System.out.println("✅ HikariCP 커넥션 풀 설정 완료:");
        System.out.println("   - URL: " + url);
        System.out.println("   - 최대 커넥션: " + config.getMaximumPoolSize());
        System.out.println("   - 최소 커넥션: " + config.getMinimumIdle());
        System.out.println("   - 배치 최적화: 활성화");
        
        return dataSource;
        
        
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.hospital.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        java.util.Properties jpaProperties = new java.util.Properties();
        
        // 기본 설정
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        jpaProperties.setProperty("hibernate.show_sql", "false");
        jpaProperties.setProperty("hibernate.format_sql", "false");
        
        // 🔹 대량 데이터 처리 최적화 설정
        jpaProperties.setProperty("hibernate.jdbc.batch_size", "100");        // 배치 크기 100개
        jpaProperties.setProperty("hibernate.order_inserts", "true");         // INSERT 순서 최적화
        jpaProperties.setProperty("hibernate.order_updates", "true");         // UPDATE 순서 최적화
        jpaProperties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        
        // 🔹 성능 최적화
        jpaProperties.setProperty("hibernate.default_batch_fetch_size", "16");
        jpaProperties.setProperty("hibernate.jdbc.fetch_size", "100");
        jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "false");
        jpaProperties.setProperty("hibernate.cache.use_query_cache", "false");
        jpaProperties.setProperty("hibernate.generate_statistics", "false");   // 통계 비활성화 (성능)
        
        // 🔹 커넥션 관리 최적화
        jpaProperties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        jpaProperties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
        
        em.setJpaProperties(jpaProperties);
        
        System.out.println("✅ JPA EntityManager 설정 완료 (대량 처리 최적화)");
        System.out.println("   - 배치 크기: 100개");
        System.out.println("   - INSERT/UPDATE 순서 최적화: 활성화");
        System.out.println("   - 2차 캐시: 비활성화 (메모리 절약)");
        
        return em;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        
        System.out.println("✅ Transaction Manager 설정 완료");
        return transactionManager;
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
}
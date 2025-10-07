package com.hospital.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Repository
@Transactional
public class CommonBatchRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final int HIBERNATE_BATCH_SIZE = 100;
    
    
    public <T> void batchInsertWithJdbc(List<T> entities, String sql, Function<T, Object[]> parameterMapper) {
        if (entities == null || entities.isEmpty()) {
            log.debug("저장할 데이터가 없습니다.");
            return;
        }
        
        String entityType = entities.get(0).getClass().getSimpleName();
        log.info("{} JDBC 배치 INSERT 시작: {}개", entityType, entities.size());
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 엔티티를 배치 파라미터로 변환
            List<Object[]> batchArgs = entities.stream()
                    .map(parameterMapper)
                    .collect(Collectors.toList());
            
            // 배치 INSERT 실행
            int[] results = jdbcTemplate.batchUpdate(sql, batchArgs);
            
            long endTime = System.currentTimeMillis();
            
            log.info("{} JDBC 배치 INSERT 완료: {}개 처리, {}ms 소요", 
                    entityType, results.length, endTime - startTime);
            
        } catch (DataAccessException e) {
            log.error("{} JDBC 배치 INSERT 실패: {}개", entityType, entities.size(), e);
            throw new RuntimeException("배치 저장 중 오류 발생: " + entityType, e);
        }
    }
    
  
    public <T> void saveBatchOptimized(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            log.debug("저장할 데이터가 없습니다.");
            return;
        }
        
        String entityType = entities.get(0).getClass().getSimpleName();
        log.info("{} JPA 최적화된 배치 저장 시작: {}개", entityType, entities.size());
        
        try {
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < entities.size(); i++) {
                entityManager.persist(entities.get(i));
                
                // Hibernate 배치 크기마다 flush & clear
                if ((i + 1) % HIBERNATE_BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    
                    if (log.isDebugEnabled()) {
                        log.debug("{} JPA 배치 flush 완료: {}개", entityType, i + 1);
                    }
                }
            }
            
            // 마지막 배치 처리
            entityManager.flush();
            entityManager.clear();
            
            long endTime = System.currentTimeMillis();
            
            log.info("{} JPA 최적화된 배치 저장 완료: {}개, {}ms 소요", 
                    entityType, entities.size(), endTime - startTime);
            
        } catch (Exception e) {
            log.error("{} JPA 배치 저장 실패: {}개", entityType, entities.size(), e);
            throw new RuntimeException("JPA 배치 저장 중 오류 발생: " + entityType, e);
        }
    }
    
  
    public <T> void saveBatchWithFallback(List<T> entities, String sql, Function<T, Object[]> parameterMapper) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        String entityType = entities.get(0).getClass().getSimpleName();
        
        try {
            // 1차 시도: 고성능 JDBC 배치 (99%는 여기서 성공)
            batchInsertWithJdbc(entities, sql, parameterMapper);
            
        } catch (Exception e) {
            log.warn("{} JDBC 배치 실패, JPA 방식으로 폴백 시도", entityType, e);
            
            try {
                // 2차 시도: JPA 최적화 배치 (1%가 여기서 처리됨)
                saveBatchOptimized(entities);
                log.info("{} JPA 폴백 저장 성공", entityType);
                
            } catch (Exception ex) {
                log.error("{} 모든 배치 저장 방식 실패", entityType, ex);
                throw new RuntimeException("모든 배치 저장 방식 실패: " + entityType, ex);
            }
        }
    }
    
   
    @Transactional
    public void executeMultipleBatchInserts(Runnable... batchOperations) {
        log.info("다중 배치 INSERT 시작: {}개 작업", batchOperations.length);
        
        try {
            for (int i = 0; i < batchOperations.length; i++) {
                log.debug("배치 INSERT {}/{} 실행", i + 1, batchOperations.length);
                batchOperations[i].run();
            }
            
            log.info("다중 배치 INSERT 완료: {}개 작업 성공", batchOperations.length);
            
        } catch (Exception e) {
            log.error("다중 배치 INSERT 실패 - 트랜잭션 롤백됩니다", e);
            throw e;
        }
    }
}
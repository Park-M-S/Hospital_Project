package com.hospital.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//전역처리 예외
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //API 호출 관련 예외들 
    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public ResponseEntity<Map<String, Object>> handleHttpException(Exception e) {
        String message = "외부 API 호출 실패: " + e.getMessage();
        log.error("x " + message, e);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createErrorResponse("API_ERROR", message));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleNetworkException(ResourceAccessException e) {
        String message = "네트워크 연결 실패: " + e.getMessage();
        log.error("x " + message, e);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createErrorResponse("NETWORK_ERROR", message));
    }

    //JSON 파싱 오류 (ObjectMapper 관련)
    @ExceptionHandler(com.fasterxml.jackson.core.JsonProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleJsonException(Exception e) {
        String message = "데이터 파싱 실패: " + e.getMessage();
        log.error("x " + message, e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("PARSING_ERROR", message));
    }

    // 데이터베이스 관련 예외들
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseException(Exception e) {
        String message = "데이터베이스 오류: " + e.getMessage();
        log.error("x " + message, e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("DATABASE_ERROR", message));
    }

    // 일반적인 RuntimeException 
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        String message = "처리 중 오류 발생: " + e.getMessage();
        log.error("x " + message, e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("RUNTIME_ERROR", message));
    }

    // 최상위 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception e) {
        String message = "시스템 오류가 발생했습니다";
        log.error("x 예상치 못한 오류: {}", e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("SYSTEM_ERROR", message));
    }

    // 응답 생성 유틸리티
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
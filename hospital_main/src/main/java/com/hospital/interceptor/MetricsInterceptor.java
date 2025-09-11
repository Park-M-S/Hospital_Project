package com.hospital.interceptor;

import com.hospital.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 안전한 메트릭 수집 인터셉터
 */
@Component
public class MetricsInterceptor implements HandlerInterceptor {

    @Autowired
    private CustomMetrics customMetrics;

    private static final String TIMER_SAMPLE = "metrics.timer";
    private static final String START_TIME = "metrics.start";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String path = getSimplePath(request);
            
            // 모니터링 엔드포인트는 제외
            if (isExcludedPath(path)) {
                return true;
            }
            
            // 타이머 시작
            Timer.Sample sample = customMetrics.startTimer();
            request.setAttribute(TIMER_SAMPLE, sample);
            request.setAttribute(START_TIME, System.currentTimeMillis());
            
            // 기본 메트릭 수집
            String method = request.getMethod();
            customMetrics.incrementRequestCount(path, method);
            customMetrics.incrementActiveUsers();
            
            // 큐 사이즈 업데이트
            long activeUsers = customMetrics.getCurrentActiveUsers();
            customMetrics.setQueueSize(Math.max(0, activeUsers - 5));
            
        } catch (Exception e) {
            // 메트릭 수집 실패가 요청 처리에 영향주지 않도록
            System.err.println("Metrics preHandle failed: " + e.getMessage());
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        try {
            String path = getSimplePath(request);
            
            // 모니터링 엔드포인트는 제외
            if (isExcludedPath(path)) {
                return;
            }
            
            String method = request.getMethod();
            int statusCode = response.getStatus();
            
            // 타이머 종료
            Timer.Sample sample = (Timer.Sample) request.getAttribute(TIMER_SAMPLE);
            if (sample != null) {
                customMetrics.recordResponseTime(sample, path, method);
            }
            
            // 성공/실패 메트릭
            if (ex != null) {
                customMetrics.incrementErrorCount(path, method, ex.getClass().getSimpleName());
            } else if (statusCode >= 400) {
                customMetrics.incrementErrorCount(path, method, "HTTP_" + statusCode);
            } else {
                customMetrics.incrementSuccessCount(path, method, statusCode);
            }
            
            // 활성 사용자 감소
            customMetrics.decrementActiveUsers();
            
            // 느린 요청 로깅
            Long startTime = (Long) request.getAttribute(START_TIME);
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                if (duration > 5000) {
                    System.out.println("Slow request: " + method + " " + path + " (" + duration + "ms)");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Metrics afterCompletion failed: " + e.getMessage());
        }
    }

    /**
     * 간단하고 안전한 경로 추출
     */
    private String getSimplePath(HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            
            if (contextPath != null && !contextPath.isEmpty()) {
                uri = uri.substring(contextPath.length());
            }
            
            // 간단한 정규화 (숫자만 {id}로 변경)
            uri = uri.replaceAll("/\\d+", "/{id}");
            
            return uri.isEmpty() ? "/" : uri;
            
        } catch (Exception e) {
            return "/unknown";
        }
    }

    /**
     * 제외할 경로 체크
     */
    private boolean isExcludedPath(String path) {
        if (path == null) return true;
        
        return path.startsWith("/actuator/") ||
               path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.endsWith(".ico") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".css") ||
               path.endsWith(".js");
    }
}

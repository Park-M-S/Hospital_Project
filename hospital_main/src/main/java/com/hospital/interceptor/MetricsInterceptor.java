package com.hospital.interceptor;

import com.hospital.metrics.CustomMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring MVC 인터셉터를 통한 자동 메트릭 수집
 */
@Component
public class MetricsInterceptor implements HandlerInterceptor {

    @Autowired
    private CustomMetrics customMetrics;

    private static final String METRICS_TIMER_SAMPLE = "metrics.timer.sample";
    private static final String METRICS_START_TIME = "metrics.start.time";

    /**
     * 요청 처리 전 실행
     * - 타이머 시작
     * - 요청 카운터 증가
     * - 활성 사용자 수 관리
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 타이머 샘플 시작
        Timer.Sample sample = customMetrics.startTimer();
        request.setAttribute(METRICS_TIMER_SAMPLE, sample);
        request.setAttribute(METRICS_START_TIME, System.currentTimeMillis());
        
        // 요청 정보 추출
        String endpoint = getEndpointFromRequest(request);
        String method = request.getMethod();
        
        // 요청 카운터 증가
        customMetrics.incrementRequestCount(endpoint, method);
        
        // 활성 연결 수 증가 (간단한 방식)
        customMetrics.incrementActiveUsers();
        
        // 특별한 엔드포인트들에 대한 추가 메트릭
        trackSpecialEndpoints(request, endpoint, method);
        
        return true;
    }

    /**
     * 뷰 렌더링 후 실행 (성공적인 처리 후)
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // 여기서는 특별한 처리가 필요없음
        // afterCompletion에서 모든 메트릭 처리
    }

    /**
     * 요청 완료 후 실행 (성공/실패 관계없이)
     * - 응답 시간 기록
     * - 성공/실패 카운터 증가
     * - 활성 사용자 수 감소
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // 타이머 샘플 종료 및 응답 시간 기록
        Timer.Sample sample = (Timer.Sample) request.getAttribute(METRICS_TIMER_SAMPLE);
        String endpoint = getEndpointFromRequest(request);
        String method = request.getMethod();
        
        if (sample != null) {
            customMetrics.recordResponseTime(sample, endpoint, method);
        }

        // 응답 상태 코드에 따른 메트릭 처리
        int statusCode = response.getStatus();
        
        if (ex != null) {
            // 예외 발생 시 에러 메트릭 증가
            String errorType = ex.getClass().getSimpleName();
            customMetrics.incrementErrorCount(endpoint, method, errorType);
        } else if (statusCode >= 400) {
            // HTTP 에러 응답 시 에러 메트릭 증가
            String errorType = "HTTP_" + statusCode;
            customMetrics.incrementErrorCount(endpoint, method, errorType);
        } else {
            // 성공 응답 시 성공 메트릭 증가
            customMetrics.incrementSuccessCount(endpoint, method, statusCode);
        }
        
        // 활성 사용자 수 감소
        customMetrics.decrementActiveUsers();
        
        // 응답 시간 로깅 (필요한 경우)
        Long startTime = (Long) request.getAttribute(METRICS_START_TIME);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            // 느린 요청에 대한 로깅 (예: 5초 이상)
            if (duration > 5000) {
                System.out.println("Slow request detected: " + method + " " + endpoint + 
                                 " took " + duration + "ms");
            }
        }
        
        // 특별한 엔드포인트들에 대한 후처리
        trackSpecialEndpointsCompletion(request, endpoint, method, statusCode, ex);
    }

    /**
     * 요청 URI에서 엔드포인트 경로 추출
     * 동적 경로 매개변수를 일반화하여 메트릭 카디널리티 제어
     */
    private String getEndpointFromRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 컨텍스트 패스 제거
        if (contextPath != null && !contextPath.isEmpty()) {
            uri = uri.substring(contextPath.length());
        }
        
        // 동적 경로 매개변수 일반화
        uri = normalizePath(uri);
        
        return uri;
    }

    /**
     * 동적 경로를 일반화하여 메트릭 카디널리티 제어
     * 예: /api/patients/12345 -> /api/patients/{id}
     */
    private String normalizePath(String path) {
        // 숫자 ID를 {id}로 대체
        path = path.replaceAll("/\\d+", "/{id}");
        
        // UUID 패턴을 {uuid}로 대체
        path = path.replaceAll("/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "/{uuid}");
        
        // 빈 경로는 루트로 처리
        if (path.isEmpty() || path.equals("/")) {
            path = "/";
        }
        
        return path;
    }

    /**
     * 특별한 엔드포인트들에 대한 추가 메트릭 수집 (요청 시작 시)
     */
    private void trackSpecialEndpoints(HttpServletRequest request, String endpoint, String method) {
        // 병원 특화 엔드포인트들 추적
        if (endpoint.startsWith("/api/patients") && "POST".equals(method)) {
            // 환자 등록 요청 - 부서 정보 추출 시도
            String department = extractDepartmentFromRequest(request);
            request.setAttribute("metrics.patient.department", department);
        }
        
        if (endpoint.startsWith("/api/appointments") && "POST".equals(method)) {
            // 예약 생성 요청
            String appointmentType = extractAppointmentTypeFromRequest(request);
            request.setAttribute("metrics.appointment.type", appointmentType);
        }
        
        if (endpoint.startsWith("/api/prescriptions") && "POST".equals(method)) {
            // 처방전 발행 요청
            String doctorId = extractDoctorIdFromRequest(request);
            request.setAttribute("metrics.prescription.doctor", doctorId);
        }
        
        if (endpoint.startsWith("/api/emergency") && "POST".equals(method)) {
            // 응급 호출 요청
            String severity = extractEmergencySeverityFromRequest(request);
            request.setAttribute("metrics.emergency.severity", severity);
        }
        
        // 특별한 API들에 대한 큐 사이즈 업데이트
        if (endpoint.startsWith("/api/")) {
            // 간단한 큐 사이즈 시뮬레이션 (실제로는 비즈니스 로직에서 관리)
            customMetrics.setQueueSize(getCurrentQueueSize());
        }
    }

    /**
     * 특별한 엔드포인트들에 대한 후처리 (요청 완료 시)
     */
    private void trackSpecialEndpointsCompletion(HttpServletRequest request, String endpoint, 
                                               String method, int statusCode, Exception ex) {
        // 성공적인 요청에 대해서만 비즈니스 메트릭 증가
        if (ex == null && statusCode >= 200 && statusCode < 400) {
            
            if (endpoint.startsWith("/api/patients") && "POST".equals(method)) {
                String department = (String) request.getAttribute("metrics.patient.department");
                customMetrics.incrementPatientRegistration(department != null ? department : "unknown");
            }
            
            if (endpoint.startsWith("/api/appointments") && "POST".equals(method)) {
                String appointmentType = (String) request.getAttribute("metrics.appointment.type");
                String department = extractDepartmentFromRequest(request);
                customMetrics.incrementAppointment(
                    appointmentType != null ? appointmentType : "unknown",
                    department != null ? department : "unknown"
                );
            }
            
            if (endpoint.startsWith("/api/prescriptions") && "POST".equals(method)) {
                String doctorId = (String) request.getAttribute("metrics.prescription.doctor");
                String department = extractDepartmentFromRequest(request);
                customMetrics.incrementPrescription(
                    doctorId != null ? doctorId : "unknown",
                    department != null ? department : "unknown"
                );
            }
            
            if (endpoint.startsWith("/api/emergency") && "POST".equals(method)) {
                String severity = (String) request.getAttribute("metrics.emergency.severity");
                customMetrics.incrementEmergencyCall(severity != null ? severity : "unknown");
            }
        }
    }

    /**
     * 요청에서 부서 정보 추출
     */
    private String extractDepartmentFromRequest(HttpServletRequest request) {
        // 쿼리 파라미터에서 부서 정보 추출
        String department = request.getParameter("department");
        if (department != null && !department.trim().isEmpty()) {
            return department.trim();
        }
        
        // 헤더에서 부서 정보 추출
        department = request.getHeader("X-Department");
        if (department != null && !department.trim().isEmpty()) {
            return department.trim();
        }
        
        // 기본값
        return "general";
    }

    /**
     * 현재 큐 사이즈 계산 (간단한 시뮬레이션)
     * 실제 환경에서는 비즈니스 로직에서 실제 큐 사이즈를 제공해야 함
     */
    private long getCurrentQueueSize() {
        // 현재 활성 사용자 수를 기반으로 한 간단한 큐 사이즈 추정
        long activeUsers = customMetrics.getCurrentActiveUsers();
        
        // 큐 사이즈는 활성 사용자 수의 일정 비율로 가정
        long estimatedQueueSize = Math.max(0, activeUsers - 10);
        
        return estimatedQueueSize;
    }

    /**
     * 제외할 경로들 체크 (모니터링 엔드포인트 등)
     */
    private boolean shouldExcludeFromMetrics(String endpoint) {
        // 모니터링 엔드포인트들은 메트릭에서 제외
        if (endpoint.startsWith("/actuator/")) {
            return true;
        }
        
        // 정적 리소스들 제외
        if (endpoint.startsWith("/static/") || 
            endpoint.startsWith("/css/") || 
            endpoint.startsWith("/js/") || 
            endpoint.startsWith("/images/") ||
            endpoint.endsWith(".ico") ||
            endpoint.endsWith(".png") ||
            endpoint.endsWith(".jpg") ||
            endpoint.endsWith(".css") ||
            endpoint.endsWith(".js")) {
            return true;
        }
        
        return false;
    }
}

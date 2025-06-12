package com.hospital.caller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.api.MedicalSubjectApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class MedicalSubjectApiCaller {

    // ✅ 진료과목 API 기본 URL
    private static final String BASE_URL = "https://apis.data.go.kr/B551182/MadmDtlInfoService2.7/";
    
    // ✅ 인코딩된 인증키 (공공데이터포털에서 발급받은 서비스 키)
    private static final String SERVICE_KEY = "TzaQUmy4fc/d/DHC1P4CuTojEFQfVTDolu1aol1Ex2KZl4NUT0guLiLeF6i0L645pR48XvVYcWCX28KPD4dzXA==";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ✅ 생성자 주입 방식 (ObjectMapper는 Bean으로 등록되어 있음)
    public MedicalSubjectApiCaller(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * ✅ 병원코드 기반 진료과목 정보 요청
     * @param apiPath - 호출할 API 경로 (예: getDgsbjtInfo2.7)
     * @param queryParams - 예: "ykiho=xxx" 형식의 쿼리 문자열
     * @return 응답 객체 (MedicalSubjectApiResponse)
     */
    public MedicalSubjectApiResponse callApi(String apiPath, String queryParams) {
        try {
            // 🔗 전체 API URL 구성
            String fullUrl = BASE_URL + apiPath + "?serviceKey=" + SERVICE_KEY + "&_type=json&" + queryParams;

            // 📡 API 호출
            String response = restTemplate.getForObject(fullUrl, String.class);

            // 📦 JSON → DTO 객체 매핑
            return objectMapper.readValue(response, MedicalSubjectApiResponse.class);

        } catch (Exception e) {
            // ❌ 예외 처리 및 로그 출력
            log.error("❌ 진료과목 API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("진료과목 API 호출 중 오류 발생", e);
        }
    }
}

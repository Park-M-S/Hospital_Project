package com.hospital.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.api.ProDocApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProDocApiCaller {

    // ✅ 공공데이터 포털 진료과목 API 기본 URL
    private static final String BASE_URL = "https://apis.data.go.kr/B551182/MadmDtlInfoService2.7/";
    
    // ✅ 인코딩된 서비스 인증 키 (공공 API 호출 시 필수)
    private static final String SERVICE_KEY = "iJsu9ygUVo24pnKXWsntyEmfZtNPVq5WoaRHYNoq7JQv0Jhq3LyRzf/P7QXb3I2Kw1i1lcRBEukiJoZfoWX56g=="; 

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ✅ 생성자 주입: ObjectMapper는 스프링이 자동 주입
    public ProDocApiCaller(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate(); // HTTP 호출용
        this.objectMapper = objectMapper;       // JSON → 객체 변환용
    }

    
    public ProDocApiResponse callApi(String apiPath, String queryParams) {
        try {
            // 🔗 최종 호출할 전체 URL 생성
            String fullUrl = BASE_URL + apiPath + "?serviceKey=" + SERVICE_KEY + "&_type=json&" + queryParams;

            // 📡 외부 API 호출 (GET 방식)
            String response = restTemplate.getForObject(fullUrl, String.class);

            // 📦 JSON 응답을 Java 객체로 역직렬화
            return objectMapper.readValue(response, ProDocApiResponse.class);

        } catch (Exception e) {
            // ❌ 예외 발생 시 로그와 함께 래핑해서 전파
            throw new RuntimeException("ProDoc API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}

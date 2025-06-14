package com.hospital.caller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.api.ProDocApiResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProDocApiCaller {

	@Value("${hospital.proDoc.api.base-url}")
	private String baseUrl;

	@Value("${hospital.proDoc.api.key}")
	private String serviceKey;
	
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

 
    public ProDocApiCaller(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate(); // HTTP 호출용
        this.objectMapper = objectMapper;       // JSON → 객체 변환용
    }

    
    public ProDocApiResponse callApi(String apiPath, String queryParams) {
        try {
            // 최종 호출할 전체 URL 생성
            String fullUrl = baseUrl + apiPath + "?serviceKey=" + serviceKey + "&_type=json&" + queryParams;

            // 외부 API 호출 (GET 방식)
            String response = restTemplate.getForObject(fullUrl, String.class);

            // JSON 응답을 Java 객체로 역직렬화
            return objectMapper.readValue(response, ProDocApiResponse.class);

        } catch (Exception e) {
            // 예외 발생 시 로그와 함께 래핑해서 전파
            throw new RuntimeException("ProDoc API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}

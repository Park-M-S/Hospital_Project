package com.hospital.caller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.ProDocApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@Component
public class ProDocApiCaller {

	@Value("${hospital.proDoc.api.base-url}")
	private String baseUrl;

	@Value("${hospital.proDoc.api.key}")
	private String serviceKey;
	
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ProDocApiCaller(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public ProDocApiResponse callApi(String queryParams) {
        try {
            // 최종 호출할 전체 URL 생성
            String fullUrl = baseUrl + "?serviceKey=" + serviceKey + "&_type=json&" + queryParams;

            // 외부 API 호출 (GET 방식)
            String response = restTemplate.getForObject(fullUrl, String.class);

            if (response == null || response.trim().isEmpty()) {
                log.warn("API 응답이 비어있음");
                return null;
            }

            log.debug("API 응답: {}", response);

            // JSON 응답을 Java 객체로 역직렬화
            return objectMapper.readValue(response, ProDocApiResponse.class);

        } catch (HttpClientErrorException e) {
            // 4xx 클라이언트 오류
            log.error("API 클라이언트 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ProDoc API 클라이언트 오류: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            // 5xx 서버 오류
            log.error("API 서버 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("ProDoc API 서버 오류: " + e.getMessage(), e);

        } catch (JsonProcessingException e) {
            // JSON 파싱 오류
            log.error("JSON 파싱 오류: {}", e.getMessage());
            throw new RuntimeException("ProDoc API JSON 파싱 오류: " + e.getMessage(), e);

        } catch (Exception e) {
            // 기타 예외
            log.error("ProDoc API 호출 중 예외 발생", e);
            throw new RuntimeException("ProDoc API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
package com.hospital.caller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.HospitalMainApiResponse;
import com.hospital.dto.ProDocApiItem;
import com.hospital.dto.ProDocApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@Component
public class ProDocApiCaller {

	@Value("${hospital.main.api.base-url}")
	private String baseUrl;

	@Value("${hospital.main.api.key}")
	private String serviceKey;
	
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ProDocApiCaller(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public ProDocApiResponse callApi(String queryParams) {
        String encodedServiceKey;
        try {
            encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("서비스 키 인코딩 실패: {}", e.getMessage());
            throw new RuntimeException("서비스 키 인코딩 실패: " + e.getMessage(), e);
        }

        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                                     .query(queryParams)
                                     .queryParam("serviceKey", encodedServiceKey)
                                     .queryParam("_type", "json")
                                     .build(true)
                                     .toUri();
        try {

            // 외부 API 호출 (GET 방식)
            String responseJson = restTemplate.getForObject(uri, String.class);

            if (responseJson == null || responseJson.trim().isEmpty()) {
                log.warn("API 응답이 비어있음");
                return null;
            }

            log.debug("API 응답: {}", responseJson);

            ProDocApiResponse apiResponseDto = objectMapper.readValue(responseJson, ProDocApiResponse.class);
            
            // resultCode 검사
            String resultCode = apiResponseDto.getResponse().getHeader().getResultCode();
            String resultMsg = apiResponseDto.getResponse().getHeader().getResultMsg();

            if (!"00".equals(resultCode)) {
                log.error("API 응답 오류 - 코드: {}, 메시지: {}", resultCode, resultMsg);
                throw new RuntimeException("API 응답 오류: " + resultCode + " - " + resultMsg);
            }

            return apiResponseDto;

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
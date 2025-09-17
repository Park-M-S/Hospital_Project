package com.hospital.caller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.HospitalMainApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class HospitalMainApiCaller {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hospital.main.api.base-url}")
    private String baseUrl;

    @Value("${hospital.main.api.key}")
    private String serviceKey;

    public HospitalMainApiCaller(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public HospitalMainApiResponse callApi(String queryParams) {
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
            
            String responseJson = restTemplate.getForObject(uri, String.class);
            
            if (responseJson == null || responseJson.trim().isEmpty()) {
                log.warn("API 응답이 비어있음");
                throw new RuntimeException("API 응답이 비어있습니다");
            }

            log.debug("API 응답: {}", responseJson);

            // JSON 파싱
            HospitalMainApiResponse apiResponseDto = objectMapper.readValue(responseJson, HospitalMainApiResponse.class);

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
            throw new RuntimeException("HospitalMain API 클라이언트 오류: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            // 5xx 서버 오류
            log.error("API 서버 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("HospitalMain API 서버 오류: " + e.getMessage(), e);

        } catch (JsonProcessingException e) {
            // JSON 파싱 오류
            log.error("JSON 파싱 오류: {}", e.getMessage());
            throw new RuntimeException("API 응답 JSON 파싱 오류: " + e.getMessage(), e);

        } catch (Exception e) {
            // 기타 예외
            log.error("HospitalMain API 호출 중 예외 발생", e);
            throw new RuntimeException("HospitalMain API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
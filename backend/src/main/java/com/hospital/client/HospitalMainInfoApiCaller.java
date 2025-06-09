
package com.hospital.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.api.HospitalMainApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URI;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class HospitalMainInfoApiCaller {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.data.go.kr.base-url}")
    private String baseUrl;

    @Value("${api.data.go.kr.service-key}")
    private String serviceKey;

    public HospitalMainInfoApiCaller(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }


    public HospitalMainApiResponse callApi(String apiPath, String queryParams) {
        String encodedServiceKey;
        try {
            encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("서비스 키 인코딩 실패: " + e.getMessage(), e);
        }

        URI uri = UriComponentsBuilder.fromUriString(baseUrl + apiPath)
                                     .query(queryParams)
                                     .queryParam("serviceKey", encodedServiceKey)
                                     .queryParam("_type", "json")
                                     .build(true)
                                     .toUri();

        String responseJson;
        try {
            responseJson = restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            throw new RuntimeException("API 호출 중 오류 발생: " + uri.toString() + ", " + e.getMessage(), e);
        }

        HospitalMainApiResponse apiResponseDto; // ★★★ DTO 객체 선언 ★★★
        try {
            // ★★★ JSON 문자열을 DTO 객체로 직접 매핑 ★★★
            apiResponseDto = objectMapper.readValue(responseJson, HospitalMainApiResponse.class);

            // resultCode 검사도 DTO 객체를 통해 수행
            String resultCode = apiResponseDto.getResponse().getHeader().getResultCode();
            String resultMsg = apiResponseDto.getResponse().getHeader().getResultMsg();

            if (!"00".equals(resultCode)) {
                throw new RuntimeException("API 응답 오류: " + resultCode + " - " + resultMsg + " (URL: " + uri.toString() + ")");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("API 응답 JSON 파싱 오류: " + e.getMessage() + " (응답: " + responseJson + ")", e);
        }

        return apiResponseDto; // ★★★ DTO 객체 반환 ★★★
    }
}
package com.hospital.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.api.HospitalDetailApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class HospitalDetailApiCaller {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hospital.detail.api.base-url}")
    private String baseUrl;

    @Value("${hospital.detail.api.key}")
    private String serviceKey;

    public HospitalDetailApiCaller(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        // XML 메시지 컨버터 추가
        this.restTemplate.getMessageConverters().add(0, new MappingJackson2XmlHttpMessageConverter());
    }

    public HospitalDetailApiResponse callApi(String hospitalCode) {

        String encodedServiceKey;
        try {
            encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("서비스 키 인코딩 실패: " + e.getMessage(), e);
        }

        // API 요청 URL 빌드
        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("serviceKey", encodedServiceKey)
                .queryParam("ykiho", hospitalCode)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1)
                .queryParam("_type", "xml")
                .build(true)
                .toUri();

        System.out.println("HospitalDetail API URL: " + uri.toString());

        HospitalDetailApiResponse apiResponseDto;
        try {
            // HttpHeaders 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_XML));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            apiResponseDto = restTemplate.exchange(
                uri, 
                HttpMethod.GET, 
                entity, 
                HospitalDetailApiResponse.class
            ).getBody();

            System.out.println("HospitalDetail API Parsed Response (XML): " + apiResponseDto);

            // DTO 구조 변경에 따른 수정: getResponse() 제거
            String resultCode = apiResponseDto.getHeader().getResultCode();
            String resultMsg = apiResponseDto.getHeader().getResultMsg();

            if (!"00".equals(resultCode)) {
                throw new RuntimeException("HospitalDetail API 응답 오류: " + resultCode + " - " + resultMsg + " (URL: " + uri.toString() + ")");
            }

        } catch (Exception e) {
            System.err.println("HospitalDetail API 호출 또는 XML 응답 파싱 중 오류 발생: " + uri.toString() + ", " + e.getMessage());
            throw new RuntimeException("HospitalDetail API 호출 또는 XML 응답 파싱 오류: " + e.getMessage(), e);
        }

        return apiResponseDto;
    }
}
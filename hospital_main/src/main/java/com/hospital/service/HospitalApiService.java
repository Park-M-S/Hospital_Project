package com.hospital.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;

@Service
public class HospitalApiService {

    private final Logger logger = LoggerFactory.getLogger(HospitalApiService.class);

    public String fetchHospitalData() {
        String serviceKey = "";
        String encodedKey = java.net.URLEncoder.encode(serviceKey, java.nio.charset.StandardCharsets.UTF_8); // 명시적 인코딩

        // 1. URI Builder 생성
        DefaultUriBuilderFactory builderFactory = new DefaultUriBuilderFactory();
        builderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); // 자동 인코딩 방지

        // 2. URI 문자열 조립
        String uriString = builderFactory.builder()
                .scheme("https")
                .host("apis.data.go.kr")
                .path("/B551182/hospInfoServicev2/getHospBasisList")
                .queryParam("ServiceKey", encodedKey)
                .queryParam("pageNo", "1")
                .queryParam("numOfRows", "10")
                .build()
                .toString();

        // 3. String → URI 변환
        URI uri = URI.create(uriString);
        
        logger.info("최종 호출 URI: {}", uri);

        // 4. 요청
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(uri, String.class);
    }
}
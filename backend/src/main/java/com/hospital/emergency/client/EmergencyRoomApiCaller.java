package com.hospital.emergency.client;

import com.hospital.emergency.dto.EmergencyRoomApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class EmergencyRoomApiCaller {

    @Value("${hospital.emergency.api.baseUrl}")
    private String baseUrl;

    @Value("${hospital.emergency.api.serviceKey}")
    private String serviceKey;

    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;

    public EmergencyRoomApiCaller(RestTemplate restTemplate, XmlMapper xmlMapper) {
        this.restTemplate = restTemplate;
        this.xmlMapper = xmlMapper;
    }

    public EmergencyRoomApiResponse callEmergencyRoomApi(double latitude, double longitude) {
        try {
            String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString())
                                                .replace("+", "%2B");

            URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/getEgytLcinfoInqire")
                    .queryParam("serviceKey", encodedServiceKey)
                    .queryParam("WGS84_LAT", latitude)
                    .queryParam("WGS84_LON", longitude)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 10)
                    .queryParam("_type", "xml")
                    .build(true)
                    .toUri();

            System.out.println("응급실 API 요청 URL: " + uri.toString());

            // HTTP 헤더 설정 - User-Agent와 Accept 헤더 명시적 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/xml, text/xml, */*");
            headers.set("Accept-Charset", "UTF-8");
            headers.set("Accept-Encoding", "gzip, deflate");
            headers.set("Accept-Encoding", "identity"); 
            
            HttpEntity<?> entity = new HttpEntity<>(headers);

            System.out.println("=== 요청 헤더 정보 ===");
            headers.forEach((key, value) -> System.out.println(key + ": " + value));

            // exchange 메소드로 헤더와 함께 요청
            ResponseEntity<String> rawResponseEntity = restTemplate.exchange(
                uri, HttpMethod.GET, entity, String.class);

            System.out.println("=== 응답 정보 ===");
            System.out.println("HTTP 상태: " + rawResponseEntity.getStatusCode());
            System.out.println("응답 헤더:");
            rawResponseEntity.getHeaders().forEach((key, value) -> 
                System.out.println("  " + key + ": " + value));

            String rawResponse = rawResponseEntity.getBody();
            
            System.out.println("=== 응답 본문 길이 ===");
            if (rawResponse != null) {
                System.out.println("응답 본문 길이: " + rawResponse.length() + " 문자");
                System.out.println("응답 본문 바이트 길이: " + rawResponse.getBytes(StandardCharsets.UTF_8).length + " 바이트");
            } else {
                System.out.println("응답 본문이 null입니다.");
            }

            System.out.println("--- 원본 API 응답 시작 ---");
            if (rawResponse != null && !rawResponse.isEmpty()) {
                // 처음 500자만 출력하여 로그가 너무 길어지지 않도록 함
                String preview = rawResponse.length() > 500 ? 
                    rawResponse.substring(0, 500) + "... (총 " + rawResponse.length() + "자)" : 
                    rawResponse;
                System.out.println(preview);
            } else {
                System.out.println("[API 응답 본문이 비어있거나 null입니다]");
            }
            System.out.println("--- 원본 API 응답 끝 ---");

            if (!rawResponseEntity.getStatusCode().is2xxSuccessful()) {
                System.err.println("API 호출 실패. HTTP 상태: " + rawResponseEntity.getStatusCode());
                return null;
            }

            // 응답이 비어있다면 여기서 리턴
            if (rawResponse == null || rawResponse.trim().isEmpty()) {
                System.err.println("API 응답 본문이 비어있습니다. 서버에서 빈 응답을 반환했습니다.");
                return null;
            }

            // XML 파싱 시도
            EmergencyRoomApiResponse apiResponse = null;
            try {
                System.out.println("XML 파싱 시작...");
                apiResponse = xmlMapper.readValue(rawResponse, EmergencyRoomApiResponse.class);
                System.out.println("XML 파싱 성공!");
            } catch (Exception parseException) {
                System.err.println("XML 파싱 실패: " + parseException.getMessage());
                parseException.printStackTrace();
                
                // 파싱 실패 시 응답의 시작 부분을 더 자세히 확인
                System.err.println("=== 파싱 실패한 응답 분석 ===");
                if (rawResponse.length() > 0) {
                    System.err.println("첫 100자: " + rawResponse.substring(0, Math.min(100, rawResponse.length())));
                    System.err.println("XML 선언 확인: " + (rawResponse.startsWith("<?xml") ? "있음" : "없음"));
                    System.err.println("루트 엘리먼트 확인: " + (rawResponse.contains("<response>") ? "response 태그 있음" : "response 태그 없음"));
                }
                return null;
            }

            if (apiResponse != null) {
                System.out.println("API 호출 성공!");
                if (apiResponse.getHeader() != null) {
                    System.out.println("API 결과 코드: " + apiResponse.getHeader().getResultCode());
                    System.out.println("API 결과 메시지: " + apiResponse.getHeader().getResultMsg());
                }
                if (apiResponse.getBody() != null) {
                    System.out.println("총 개수: " + apiResponse.getBody().getTotalCount());
                    System.out.println("페이지 번호: " + apiResponse.getBody().getPageNo());
                    System.out.println("페이지 크기: " + apiResponse.getBody().getNumOfRows());
                } else {
                    System.err.println("응답 body가 null입니다.");
                }
            }
            
            return apiResponse;

        } catch (Exception e) {
            System.err.println("응급실 API 호출 중 예외 발생: " + e.getClass().getName());
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
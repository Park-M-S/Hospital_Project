package com.hospital.emergency.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hospital.websocket.EmergencyApiWebSocketHandler;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmergencyApiService {

    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;
    private final ObjectMapper jsonMapper;

    @Value("${hospital.emergency.api.baseUrl}")
    private String baseUrl;

    @Value("${hospital.emergency.api.serviceKey}")
    private String serviceKey;
    
    @Autowired
    private EmergencyApiWebSocketHandler webSocketHandler;

    
    

    public EmergencyApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.xmlMapper = new XmlMapper();
        this.jsonMapper = new ObjectMapper();
        
        
    }
    
    // 스케줄링 메서드 추가 - 30초마다 실행
    //@Scheduled(fixedRate = 30000)
    public void updateEmergencyRoomData() {
        try {
            System.out.println("응급실 데이터 업데이트 시작...");
            String apiData = callEmergencyApi();
            
            if (apiData != null && !apiData.trim().isEmpty()) {
                webSocketHandler.broadcastEmergencyRoomData(apiData);
                System.out.println("응급실 데이터 WebSocket 브로드캐스트 완료");
            } else {
                System.out.println("API 응답 데이터가 비어있습니다.");
            }
            
        } catch (Exception e) {
            System.err.println("응급실 데이터 업데이트 중 오류 발생:");
            e.printStackTrace();
        }
    }
    
    // 초기 데이터 가져오기용 메서드 추가
    public String getEmergencyRoomData() {
        return callEmergencyApi();
    }
    

    public String callEmergencyApi() {
        String stage1 = "성남시";
        int pageNo = 1;
        int numOfRows = 10;

        try {
            // URL 파라미터 인코딩
            String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
            String encodedStage1 = URLEncoder.encode(stage1, StandardCharsets.UTF_8.toString());

            // URI 구성
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/getEmrrmRltmUsefulSckbdInfoInqire")
                    .queryParam("serviceKey", encodedServiceKey)
                    .queryParam("STAGE1", encodedStage1)
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("_type", "xml")
                    .build(true)
                    .toUri();

            System.out.println("요청 URI: " + uri.toString());

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.add("Accept", "application/xml, text/xml");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            // 응답 내용 로깅 (디버깅용)
            System.out.println("응답 Content-Type: " + response.getHeaders().getContentType());
            System.out.println("응답 시작 200자: " + responseBody.substring(0, Math.min(responseBody.length(), 200)));

            // 응답이 JSON인지 XML인지 확인
            String trimmedResponse = responseBody.trim();
            
            if (trimmedResponse.startsWith("{") || trimmedResponse.startsWith("[")) {
                // JSON 응답인 경우
                System.out.println("JSON 응답을 받았습니다.");
                
                // JSON을 파싱하여 예쁘게 출력
                JsonNode jsonNode = jsonMapper.readTree(responseBody);
                return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
                
            } else if (trimmedResponse.startsWith("<")) {
                // XML 응답인 경우
                System.out.println("XML 응답을 받았습니다.");
                
                // XML을 JsonNode로 변환 후 JSON 문자열로 반환
                JsonNode xmlNode = xmlMapper.readTree(responseBody.getBytes(StandardCharsets.UTF_8));
                return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xmlNode);
                
            } else {
                // 예상하지 못한 형태의 응답
                System.out.println("예상하지 못한 응답 형태입니다: " + trimmedResponse.substring(0, Math.min(50, trimmedResponse.length())));
                
                // 일단 원본 응답을 그대로 반환
                return responseBody;
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL 인코딩 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("API 호출 또는 변환 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // API 응답을 원본 그대로 반환하는 메서드 (디버깅용)
    public String callEmergencyApiRaw() {
        String stage1 = "성남시";
        int pageNo = 1;
        int numOfRows = 10;

        try {
            String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
            String encodedStage1 = URLEncoder.encode(stage1, StandardCharsets.UTF_8.toString());

            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/getEmrrmRltmUsefulSckbdInfoInqire")
                    .queryParam("serviceKey", encodedServiceKey)
                    .queryParam("STAGE1", encodedStage1)
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("_type", "xml")
                    .build(true)
                    .toUri();

            return restTemplate.getForObject(uri, String.class);

        } catch (Exception e) {
            throw new RuntimeException("API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
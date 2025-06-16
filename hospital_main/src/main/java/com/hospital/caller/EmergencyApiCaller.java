package com.hospital.caller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class EmergencyApiCaller {

    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper;
    private final ObjectMapper jsonMapper;

    @Value("${hospital.emergency.api.baseUrl}")
    private String baseUrl;

    @Value("${hospital.emergency.api.serviceKey}")
    private String serviceKey;

    public EmergencyApiCaller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.xmlMapper = new XmlMapper();
        this.jsonMapper = new ObjectMapper();
    }

    public JsonNode callEmergencyApiAsJsonNode(String stage1, int pageNo, int numOfRows) {
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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.add("Accept", "application/xml, text/xml");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            String trimmed = responseBody.trim();
            if (trimmed.startsWith("<")) {
                return xmlMapper.readTree(responseBody.getBytes(StandardCharsets.UTF_8));
            } else if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return jsonMapper.readTree(responseBody);
            } else {
                throw new RuntimeException("예상하지 못한 응답 형식입니다.");
            }

        } catch (Exception e) {
            throw new RuntimeException("API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }
}

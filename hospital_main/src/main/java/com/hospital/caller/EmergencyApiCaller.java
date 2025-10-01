package com.hospital.caller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.hospital.config.RegionConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmergencyApiCaller {

	private final RestTemplate restTemplate;
	private final XmlMapper xmlMapper;
	private final ObjectMapper jsonMapper;
	private final RegionConfig regionConfig;

	@Value("${hospital.emergency.api.baseUrl}")
	private String baseUrl;

	@Value("${hospital.emergency.api.serviceKey}")
	private String serviceKey;

	public EmergencyApiCaller(RestTemplate restTemplate, RegionConfig regionConfig) {
		this.restTemplate = restTemplate;
		this.xmlMapper = new XmlMapper();
		this.jsonMapper = new ObjectMapper();
		this.regionConfig = regionConfig;
	}

	public List<JsonNode> callEmergencyApiByCityPage(String city, int pageNo, int numOfRows) {
	    try {
	        String encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8.toString());
	        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());

	        URI uri = UriComponentsBuilder.fromUriString(baseUrl)
	                .queryParam("serviceKey", encodedServiceKey)
	                .queryParam("STAGE1", encodedCity)
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

	        if (responseBody == null || responseBody.isEmpty()) return List.of();

	        JsonNode node;
	        String trimmed = responseBody.trim();
	        if (trimmed.startsWith("<")) {
	            node = xmlMapper.readTree(responseBody.getBytes(StandardCharsets.UTF_8));
	        } else if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
	            node = jsonMapper.readTree(responseBody);
	        } else {
	            return List.of();
	        }

	        return List.of(node);

	    } catch (Exception e) {
	        throw new RuntimeException("API 호출 중 오류 발생(city: " + city + ", page: " + pageNo + "): " + e.getMessage(), e);
	    }
	}
}

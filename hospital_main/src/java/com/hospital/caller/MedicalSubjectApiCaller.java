package com.hospital.caller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.MedicalSubjectApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Component
@Slf4j
public class MedicalSubjectApiCaller {

	@Value("${hospital.medicalSubject.api.base-url}")
	private String baseUrl;

	@Value("${hospital.medicalSubject.api.key}")
	private String serviceKey;

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	public MedicalSubjectApiCaller(ObjectMapper objectMapper) {
		this.restTemplate = new RestTemplate();
		this.objectMapper = objectMapper;
	}

	// 병원코드 기반 진료과목 정보 요청
	public MedicalSubjectApiResponse callApi(String queryParams) {
		try {
			// 전체 API URL 구성
			String fullUrl = baseUrl + "?serviceKey=" + serviceKey + "&_type=json&" + queryParams;

			// API 호출
			String response = restTemplate.getForObject(fullUrl, String.class);

			if (response == null || response.trim().isEmpty()) {
				log.warn("API 응답이 비어있음");
				return null;
			}

			log.debug("API 응답: {}", response);

			// JSON → DTO 객체 매핑
			return objectMapper.readValue(response, MedicalSubjectApiResponse.class);

		} catch (HttpClientErrorException e) {
			// 4xx 클라이언트 오류
			log.error("API 클라이언트 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new RuntimeException("MedicalSubject API 클라이언트 오류: " + e.getMessage(), e);

		} catch (HttpServerErrorException e) {
			// 5xx 서버 오류
			log.error("API 서버 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new RuntimeException("MedicalSubject API 서버 오류: " + e.getMessage(), e);

		} catch (JsonProcessingException e) {
			// JSON 파싱 오류
			log.error("JSON 파싱 오류: {}", e.getMessage());
			throw new RuntimeException("MedicalSubject API JSON 파싱 오류: " + e.getMessage(), e);

		} catch (Exception e) {
			// 기타 예외
			log.error("진료과목 API 호출 중 예외 발생", e);
			throw new RuntimeException("진료과목 API 호출 중 오류 발생: " + e.getMessage(), e);
		}
	}
}
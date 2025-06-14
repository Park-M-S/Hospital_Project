package com.hospital.caller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.api.HospitalDetailApiResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@Component
public class HospitalDetailApiCaller {

	@Value("${hospital.detail.api.base-url}")
	private String baseUrl;

	@Value("${hospital.detail.api.key}")
	private String serviceKey;

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	// ✅ 생성자 주입: ObjectMapper는 스프링이 자동 주입
	public HospitalDetailApiCaller(ObjectMapper objectMapper) {
		this.restTemplate = new RestTemplate(); // HTTP 호출용
		this.objectMapper = objectMapper; // JSON → 객체 변환용
	}

	public HospitalDetailApiResponse callApi(String apiPath, String queryParams) {
		try {
			// 🔗 최종 호출할 전체 URL 생성
			String fullUrl = baseUrl + apiPath + "?serviceKey=" + serviceKey + "&_type=json&" + queryParams;

			log.debug("API 호출 URL: {}", fullUrl);

			// 📡 외부 API 호출 (GET 방식)
			String response = restTemplate.getForObject(fullUrl, String.class);

			if (response == null || response.trim().isEmpty()) {
				log.warn("API 응답이 비어있음");
				return null;
			}

			log.debug("API 응답: {}", response);

			// 📦 JSON 응답을 Java 객체로 역직렬화
			return objectMapper.readValue(response, HospitalDetailApiResponse.class);

		} catch (HttpClientErrorException e) {
			// 4xx 클라이언트 오류 (잘못된 요청, 인증 실패 등)
			log.error("API 클라이언트 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new RuntimeException("HospitalDetail API 클라이언트 오류: " + e.getMessage(), e);

		} catch (HttpServerErrorException e) {
			// 5xx 서버 오류
			log.error("API 서버 오류 - 상태코드: {}, 응답: {}", e.getStatusCode(), e.getResponseBodyAsString());
			throw new RuntimeException("HospitalDetail API 서버 오류: " + e.getMessage(), e);

		} catch (Exception e) {
			// 기타 예외 (JSON 파싱 오류, 네트워크 오류 등)
			log.error("HospitalDetail API 호출 중 예외 발생", e);
			throw new RuntimeException("HospitalDetail API 호출 중 오류 발생: " + e.getMessage(), e);
		}
	}
}
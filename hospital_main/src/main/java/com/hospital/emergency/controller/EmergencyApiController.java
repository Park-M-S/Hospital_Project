package com.hospital.emergency.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.emergency.service.EmergencyApiService;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyApiController {

	private final EmergencyApiService emergencyApiService;
	private final ObjectMapper objectMapper;

	public EmergencyApiController(EmergencyApiService emergencyApiService, ObjectMapper objectMapper) {
		this.emergencyApiService = emergencyApiService;
		this.objectMapper = objectMapper;
		
	}

	@GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getEmergencyInfo() {
		Map<String, Object> response = new HashMap<>();

		try {
			// 서비스에서 JSON 문자열을 받아옴
			String jsonString = emergencyApiService.callEmergencyApi();

			// JSON 문자열을 JsonNode로 파싱
			JsonNode jsonNode = objectMapper.readTree(jsonString);

			response.put("success", true);
			response.put("message", "응급실 정보 조회 성공");
			response.put("data", jsonNode); // JsonNode 객체로 반환
			response.put("timestamp", System.currentTimeMillis());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("응급실 정보 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();

			response.put("success", false);
			response.put("message", "응급실 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
			response.put("error", e.getClass().getSimpleName());
			response.put("timestamp", System.currentTimeMillis());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}

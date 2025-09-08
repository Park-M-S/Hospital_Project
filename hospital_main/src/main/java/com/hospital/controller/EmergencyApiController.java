package com.hospital.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.dto.EmergencyWebResponse;
import com.hospital.service.EmergencyApiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/emergency")
public class EmergencyApiController {

	private final EmergencyApiService emergencyApiService;

	public EmergencyApiController(EmergencyApiService emergencyApiService) {
		this.emergencyApiService = emergencyApiService;
	}

	@GetMapping(value = "/start", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getEmergencyList() {
		log.info("응급실 정보 조회 시작...");
		
		// 스케줄러 시작
		emergencyApiService.startScheduler();
		
		// 응급실 데이터 수집
		List<EmergencyWebResponse> emergencyList = emergencyApiService.getEmergencyRoomDataAsDto();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "응급실 정보 조회 성공");
		response.put("data", emergencyList);
		response.put("count", emergencyList.size());
		response.put("note", "WebSocket으로 실시간 업데이트 제공");
		response.put("timestamp", LocalDateTime.now());
		
		log.info("응급실 정보 {}건 조회 완료", emergencyList.size());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/stop")
	public ResponseEntity<Map<String, Object>> shutdownCompleteService() {
		log.info("응급실 서비스 종료 요청...");
		
		emergencyApiService.shutdownCompleteService();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "응급실 서비스 완전 종료 완료");
		response.put("timestamp", LocalDateTime.now());
		
		log.info("응급실 서비스 종료 완료");
		return ResponseEntity.ok(response);
	}
}
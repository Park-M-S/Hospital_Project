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

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.dto.EmergencyWebResponse;
import com.hospital.service.EmergencyApiService;
import com.hospital.websocket.EmergencyApiWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/emergency")
public class EmergencyApiController {

	private final EmergencyApiService emergencyApiService;
	private final EmergencyApiWebSocketHandler emergencyApiWebSocketHandler;

	public EmergencyApiController(EmergencyApiService emergencyApiService, EmergencyApiWebSocketHandler emergencyApiWebSocketHandler) {
		this.emergencyApiService = emergencyApiService;
		this.emergencyApiWebSocketHandler = emergencyApiWebSocketHandler;
	}

	@GetMapping(value = "/start", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getEmergencyList() {
	    log.info("응급실 정보 조회 시작...");

	    // 캐시된 최신 데이터 가져오기 (스케줄러는 WebSocket에서 자동 관리)
	    JsonNode emergencyData = emergencyApiService.getEmergencyRoomData();

	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("message", "응급실 정보 조회 성공");
	    response.put("data", emergencyData);
	    response.put("count", emergencyData.size());
	    
	    // 스케줄러 상태 정보 추가
	    boolean schedulerRunning = emergencyApiService.isSchedulerRunning();
	    int connectedSessions = emergencyApiWebSocketHandler.getConnectedSessionCount();
	    
	    response.put("schedulerRunning", schedulerRunning);
	    response.put("connectedWebSocketSessions", connectedSessions);
	    
	    if (schedulerRunning) {
	        response.put("note", "WebSocket 연결이 있어 실시간 데이터 수집 중");
	    } else {
	        response.put("note", "WebSocket 연결 없음 - 캐시된 데이터 제공");
	    }
	    
	    response.put("timestamp", LocalDateTime.now());

	    log.info("응급실 정보 조회 완료 ({}건, 스케줄러: {}, WebSocket 세션: {})", 
	        emergencyData.size(), schedulerRunning, connectedSessions);
	        
	    return ResponseEntity.ok(response);
	}

	@GetMapping("/stop")
	public ResponseEntity<Map<String, Object>> shutdownCompleteService() {
	    log.info("응급실 서비스 강제 종료 요청...");

	    // 모든 WebSocket 세션 강제 종료 (이로 인해 자동으로 스케줄러도 중지됨)
	    emergencyApiWebSocketHandler.closeAllSessions();
	    
	    // 혹시 남아있을 스케줄러 강제 중지
	    emergencyApiService.stopScheduler();

	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("message", "응급실 서비스 강제 종료 완료 - 모든 WebSocket 연결 해제 및 스케줄러 중지");
	    response.put("warning", "이 API는 관리자용입니다. 정상적인 종료는 WebSocket 연결 해제로 자동 처리됩니다.");
	    response.put("timestamp", LocalDateTime.now());

	    log.info("응급실 서비스 강제 종료 완료");
	    return ResponseEntity.ok(response);
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> getServiceStatus() {
	    log.info("응급실 서비스 상태 조회...");

	    boolean schedulerRunning = emergencyApiService.isSchedulerRunning();
	    int connectedSessions = emergencyApiWebSocketHandler.getConnectedSessionCount();
	    Map<String, Object> stats = emergencyApiService.getStats();
	    
	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("schedulerRunning", schedulerRunning);
	    response.put("connectedWebSocketSessions", connectedSessions);
	    response.put("connectionStatus", emergencyApiWebSocketHandler.getConnectionStatus());
	    response.put("collectionStats", stats);
	    response.put("lastDataCount", emergencyApiService.getEmergencyRoomData().size());
	    response.put("timestamp", LocalDateTime.now());

	    if (schedulerRunning && connectedSessions > 0) {
	        response.put("status", "ACTIVE");
	        response.put("message", "정상 운영 중 - 실시간 데이터 수집 및 브로드캐스트");
	    } else if (connectedSessions > 0) {
	        response.put("status", "STARTING");
	        response.put("message", "WebSocket 연결 있음 - 스케줄러 시작 중");
	    } else {
	        response.put("status", "IDLE");
	        response.put("message", "대기 중 - WebSocket 연결 없음");
	    }

	    log.info("응급실 서비스 상태: {}, 세션: {}, 스케줄러: {}", 
	        response.get("status"), connectedSessions, schedulerRunning);
	        
	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/manual-start")
	public ResponseEntity<String> manualStart() {
	    emergencyApiService.onWebSocketConnected();
	    return ResponseEntity.ok("수동으로 스케줄러 시작됨");
	}
}
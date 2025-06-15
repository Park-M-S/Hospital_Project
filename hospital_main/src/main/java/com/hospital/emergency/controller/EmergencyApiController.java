package com.hospital.emergency.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import com.hospital.dto.web.EmergencyResponse;
import com.hospital.emergency.service.EmergencyApiService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/emergency")
public class EmergencyApiController {

	private final EmergencyApiService emergencyApiService;


	public EmergencyApiController(EmergencyApiService emergencyApiService) {
		this.emergencyApiService = emergencyApiService;

		
	}

	@GetMapping(value = "/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmergencyResponse>> getEmergencyList() {
        try {
            // 스케줄러 시작
            emergencyApiService.startScheduler();
            
            // 응급실 데이터 업데이트
            emergencyApiService.updateEmergencyRoomData();
            
            // ✅ 순수한 배열만 반환
            List<EmergencyResponse> emergencyList = emergencyApiService.getEmergencyRoomDataAsDto();
            
            return ResponseEntity.ok(emergencyList);
        } catch (Exception e) {
            System.err.println("응급실 정보 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
	

	/*
	 * 완전 서비스 종료 (스케줄러 + WebSocket)
	 */
	@GetMapping("/stop")
	public ResponseEntity<String> shutdownCompleteService() {
	    try {
	        emergencyApiService.shutdownCompleteService();
	        return ResponseEntity.ok("✅ 응급실 서비스 완전 종료 완료");
	    } catch (Exception e) {
	        return ResponseEntity.status(500).body("❌ 서비스 종료 실패: " + e.getMessage());
	    }
    }
}

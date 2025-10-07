package com.hospital.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.dto.EmergencyWebResponse;
import com.hospital.service.EmergencyMockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency/mock")
public class EmergencyMockController {

    private final EmergencyMockService mockService;

    @Autowired
    public EmergencyMockController(EmergencyMockService mockService) {
        this.mockService = mockService;
    }

    /**
     * Mock 응급실 데이터 조회 (JSON)
     */
    @GetMapping("/data")
    public ResponseEntity<?> getMockEmergencyData() {
        
        return ResponseEntity.ok(mockService.getMockEmergencyRoomData());
    }

    /**
     * Mock 응급실 데이터 조회 (List<DTO>)
     */
    @GetMapping("/list")
    public ResponseEntity<List<EmergencyWebResponse>> getMockEmergencyList() {
     
        
        List<EmergencyWebResponse> mockData = mockService.getMockDataDirect();
        return ResponseEntity.ok(mockData);
    }

 

    /**
     * Mock 스케줄러 시작
     */
    @GetMapping(value = "/emergenciesData", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> startMockScheduler() {
        // 스케줄러 시작
        mockService.onMockWebSocketConnected();
        
        // 초기 데이터도 함께 반환
        List<EmergencyWebResponse> mockData = mockService.getMockDataDirect();

        return ResponseEntity.ok(Map.of(
            "message", "Mock 스케줄러 시작됨",
            "status", "running",
            "data", mockData,
            "count", mockData.size()
        ));
    }

    /**
     * Mock 스케줄러 중지
     */
    @GetMapping("/stop")
    public ResponseEntity<Map<String, String>> stopMockScheduler() {
        mockService.stopMockScheduler();
        
        return ResponseEntity.ok(Map.of(
            "message", "Mock 스케줄러 중지됨",
            "status", "stopped"
        ));
    }

    /**
     * 지역별 응급실 개수 조회
     */
    @GetMapping("/count-by-region")
    public ResponseEntity<Map<String, Long>> getCountByRegion() {
      
        
        List<EmergencyWebResponse> mockData = mockService.getMockDataDirect();
        
        // 지역별 개수 계산 (주소에서 첫 번째 단어 기준)
        Map<String, Long> regionCount = mockData.stream()
            .filter(emergency -> emergency.getEmergencyAddress() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                emergency -> emergency.getEmergencyAddress().split(" ")[0],
                java.util.stream.Collectors.counting()
            ));
        
        return ResponseEntity.ok(regionCount);
    }

    
  
}
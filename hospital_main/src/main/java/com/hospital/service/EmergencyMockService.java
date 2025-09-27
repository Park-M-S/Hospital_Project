package com.hospital.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.EmergencyWebResponse;
import com.hospital.mock.EmergencyMockDataGenerator;
import com.hospital.websocket.EmergencyApiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EmergencyMockService {

    private final EmergencyMockDataGenerator mockDataGenerator;
    private final EmergencyApiWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;
    private volatile String latestEmergencyJson = null;
    private final AtomicBoolean schedulerRunning = new AtomicBoolean(false);
    

    @Autowired
    @Lazy
    public EmergencyMockService(EmergencyMockDataGenerator mockDataGenerator,
                               EmergencyApiWebSocketHandler webSocketHandler) {
        this.mockDataGenerator = mockDataGenerator;
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Mock WebSocket 연결 시 호출 - 첫 번째 연결이면 스케줄러 시작
     */
    public void onMockWebSocketConnected() {
     
        
        if (schedulerRunning.compareAndSet(false, true)) {
            System.out.println("🔧 Mock 모드 활성화 - Mock 데이터 사용");
            
            // MockDataGenerator의 스케줄러 활성화
            mockDataGenerator.enableScheduler();
            
            // 서비스 레벨 스케줄러도 시작
            startMockDataScheduler();
        }
    }

    /**
     * Mock WebSocket 연결 해제 시 호출 - 마지막 연결이면 스케줄러 중지
     */
    public void onMockWebSocketDisconnected() {
        if (webSocketHandler.getConnectedSessionCount() == 0) {
            if (schedulerRunning.compareAndSet(true, false)) {
                // Mock 모드에서는 별도 중지 로직 없음 (스케줄러가 자동 실행)
                System.out.println("🔧 Mock 모드 - 스케줄러는 계속 실행됨");
            }
        }
    }

    /**
     * Mock 데이터 스케줄러 시작 (30초마다 자동 갱신)
     */
    private void startMockDataScheduler() {
        // 초기 데이터 로드
        updateMockDataCache();
        
        // 30초마다 Mock 데이터 갱신하는 별도 스레드
        new Thread(() -> {
            while (schedulerRunning.get()) {
                try {
                    Thread.sleep(30000); // 30초 대기
                    if (schedulerRunning.get()) {
                        // MockDataGenerator의 메서드를 직접 호출
                        mockDataGenerator.generateRandomEmergencyData();
                        updateMockDataCache();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Mock 스케줄러 실행 중 오류: " + e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Mock 데이터 캐시 업데이트
     */
    private void updateMockDataCache() {
        try {
            List<EmergencyWebResponse> mockData = mockDataGenerator.getCachedEmergencyData();
            updateCacheFromMockResults(mockData);
            System.out.println("🔧 Mock 응급실 데이터 업데이트 완료: " + mockData.size() + "건");
        } catch (Exception e) {
            System.err.println("Mock 응급실 데이터 처리 중 오류 발생");
            e.printStackTrace();
        }
    }

    /**
     * Mock 데이터를 캐시에 저장하고 WebSocket으로 브로드캐스트
     */
    public void updateCacheFromMockResults(List<EmergencyWebResponse> dtoList) {
        if (!schedulerRunning.get() || dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        try {
            String newJsonData = objectMapper.writeValueAsString(dtoList);
            
            // 데이터가 변경된 경우에만 브로드캐스트
            if (!newJsonData.equals(latestEmergencyJson)) {
                latestEmergencyJson = newJsonData;
                webSocketHandler.broadcastEmergencyRoomData(newJsonData);
                System.out.println("✅ Mock 응급실 데이터 업데이트 및 브로드캐스트 완료");
            }
        } catch (Exception e) {
            System.err.println("Mock 응급실 데이터 처리 중 오류 발생");
            e.printStackTrace();
        }
    }

    /**
     * Mock WebSocket 초기 연결 시 캐시 반환
     */
    public JsonNode getMockEmergencyRoomData() {
     
        
        if (latestEmergencyJson == null) {
            // 초기 데이터가 없으면 즉시 생성
            updateMockDataCache();
            
            if (latestEmergencyJson == null) {
                return objectMapper.createObjectNode();
            }
        }
        
        try {
            return objectMapper.readTree(latestEmergencyJson);
        } catch (Exception e) {
            System.err.println("Mock 응급실 데이터 파싱 중 오류 발생");
            e.printStackTrace();
            return objectMapper.createObjectNode();
        }
    }
    
    /**
     * Mock 스케줄러 강제 중지
     */
    public void stopMockScheduler() {
        if (schedulerRunning.compareAndSet(true, false)) {
            // MockDataGenerator의 스케줄러도 비활성화
            mockDataGenerator.disableScheduler();
            System.out.println("✅ Mock 응급실 스케줄러 강제 중지 완료");
        } else {
            System.out.println("⚠️ Mock 스케줄러가 이미 중지되어 있습니다.");
        }
    }

    /**
     * Mock 서비스 상태 정보 반환
     */
    public Map<String, Object> getMockStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("schedulerRunning", schedulerRunning.get());
        stats.put("hasLatestData", latestEmergencyJson != null);
        stats.put("lastDataSize", getMockEmergencyRoomData().size());
        stats.put("connectedSessions", webSocketHandler.getConnectedSessionCount());
        stats.put("mockDataCount", mockDataGenerator.getCachedEmergencyData().size());
        stats.put("dataSource", "Mock Generator");
        
        return stats;
    }

    /**
     * Mock 스케줄러 상태 확인
     */
    public boolean isMockSchedulerRunning() {
        return schedulerRunning.get();
    }
    

    /**
     * Mock 데이터 즉시 조회 (캐시 무시)
     */
    public List<EmergencyWebResponse> getMockDataDirect() {
        return mockDataGenerator.getCachedEmergencyData();
    }
    
    /**
     * Mock 데이터 총 개수 조회
     */
    public int getMockDataCount() {
        return mockDataGenerator.getCachedEmergencyData().size();
    }
}
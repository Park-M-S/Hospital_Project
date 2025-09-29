package com.hospital.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.async.EmergencyAsyncRunner;
import com.hospital.dto.EmergencyWebResponse;
import com.hospital.websocket.EmergencyApiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EmergencyApiService {

    private final EmergencyAsyncRunner asyncRunner;
    private final EmergencyApiWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;
    private volatile String latestEmergencyJson = null;
    private final AtomicBoolean schedulerRunning = new AtomicBoolean(false);

    @Autowired
    @Lazy
    public EmergencyApiService(EmergencyAsyncRunner asyncRunner, 
                              EmergencyApiWebSocketHandler webSocketHandler) {
        this.asyncRunner = asyncRunner;
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * WebSocket 연결 시 호출 - 첫 번째 연결이면 스케줄러 시작
     */
    public void onWebSocketConnected() {
        if (schedulerRunning.compareAndSet(false, true)) {
            asyncRunner.runAsyncForAllCities(this::updateCacheFromAsyncResults);
            System.out.println("✅ 응급실 Async 스케줄러 시작 (첫 번째 연결)");
        }
    }

    /**
     * WebSocket 연결 해제 시 호출 - 마지막 연결이면 스케줄러 중지
     */
    public void onWebSocketDisconnected() {
        if (webSocketHandler.getConnectedSessionCount() == 0) {
            if (schedulerRunning.compareAndSet(true, false)) {
                asyncRunner.stopAsync();
                System.out.println("✅ 응급실 Async 스케줄러 종료 (마지막 연결 해제)");
            }
        }
    }

    /**
     * Async에서 처리한 DTO 리스트를 캐시에 저장하고 WebSocket으로 브로드캐스트
     */
    public void updateCacheFromAsyncResults(List<EmergencyWebResponse> dtoList) {
        if (!schedulerRunning.get() || dtoList == null || dtoList.isEmpty()) {
            return;
        }
        
        try {
            String newJsonData = objectMapper.writeValueAsString(dtoList);
            
            // 데이터가 변경된 경우에만 브로드캐스트
            if (!newJsonData.equals(latestEmergencyJson)) {
                latestEmergencyJson = newJsonData;
                webSocketHandler.broadcastEmergencyRoomData(newJsonData);
                System.out.println("✅ 응급실 데이터 업데이트 및 브로드캐스트 완료");
            }
        } catch (Exception e) {
            System.err.println("응급실 데이터 처리 중 오류 발생");
            e.printStackTrace();
        }
    }

    /**
     * WebSocket 초기 연결 시 캐시 반환
     */
    public JsonNode getEmergencyRoomData() {
        if (latestEmergencyJson == null) {
            return objectMapper.createObjectNode();
        }
        
        try {
            return objectMapper.readTree(latestEmergencyJson);
        } catch (Exception e) {
            System.err.println("응급실 데이터 파싱 중 오류 발생");
            e.printStackTrace();
            return objectMapper.createObjectNode();
        }
    }
    
    /**
     * 스케줄러 강제 중지
     */
    public void stopScheduler() {
        if (schedulerRunning.compareAndSet(true, false)) {
            asyncRunner.stopAsync();
            System.out.println("✅ 응급실 스케줄러 강제 중지 완료");
        } else {
            System.out.println("⚠️ 스케줄러가 이미 중지되어 있습니다.");
        }
    }

    /**
     * 서비스 상태 정보 반환
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("schedulerRunning", schedulerRunning.get());
        stats.put("hasLatestData", latestEmergencyJson != null);
        stats.put("lastDataSize", getEmergencyRoomData().size());
        stats.put("connectedSessions", webSocketHandler.getConnectedSessionCount());
        
        // AsyncRunner에서 통계 가져오기 (있다면)
        stats.put("completedCount", asyncRunner.getCompletedCount());
        stats.put("failedCount", asyncRunner.getFailedCount());
        stats.put("processedCount", asyncRunner.getProcessedCount());
        
        return stats;
    }

    /**
     * 스케줄러 상태 확인
     */
    public boolean isSchedulerRunning() {
        return schedulerRunning.get();
    }
}
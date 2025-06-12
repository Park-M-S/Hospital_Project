package com.hospital.emergency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.emergency.parser.EmergencyApiParser;
import com.hospital.websocket.EmergencyApiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EmergencyApiService {

    private final AtomicBoolean schedulerRunning = new AtomicBoolean(false);
    private final ObjectMapper jsonMapper;
    private final EmergencyApiParser apiParser;

    @Autowired
    private EmergencyApiWebSocketHandler webSocketHandler;

    public EmergencyApiService(EmergencyApiParser apiParser) {
        this.apiParser = apiParser;
        this.jsonMapper = new ObjectMapper();
    }

    @Scheduled(fixedRate = 30000)
    public void updateEmergencyRoomData() {
        if (!schedulerRunning.get()) return;

        try {
            System.out.println("응급실 데이터 업데이트 시작...");
            JsonNode data = apiParser.callEmergencyApiAsJsonNode("성남시", 1, 10);
            String jsonString = jsonMapper.writeValueAsString(data);

            if (data != null && !data.isEmpty()) {
                webSocketHandler.broadcastEmergencyRoomData(jsonString);
                System.out.println("응급실 데이터 WebSocket 브로드캐스트 완료");
            } else {
                System.out.println("API 응답 데이터가 비어있습니다.");
            }
        } catch (Exception e) {
            System.err.println("응급실 데이터 업데이트 중 오류 발생:");
            e.printStackTrace();
        }
    }

    public void startScheduler() {
        schedulerRunning.set(true);
    }

    public void stopScheduler() {
        schedulerRunning.set(false);
    }

    public JsonNode getEmergencyRoomData() {
        return apiParser.callEmergencyApiAsJsonNode("성남시", 1, 10);
    }
}

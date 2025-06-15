package com.hospital.emergency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.hospital.config.RegionConfig;
import com.hospital.dto.web.EmergencyResponse;
import com.hospital.emergency.caller.EmergencyApiCaller;
import com.hospital.entity.HospitalMain;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.websocket.EmergencyApiWebSocketHandler;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EmergencyApiService {

    private final AtomicBoolean schedulerRunning = new AtomicBoolean(false);
    private final ObjectMapper objectMapper;
    private final EmergencyApiCaller emergencyApiCaller;
    private final HospitalMainApiRepository hospitalMainApiRepository;
    private final RegionConfig regionConfig; 

    private ScheduledFuture<?> scheduledTask;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private EmergencyApiWebSocketHandler webSocketHandler;

    public EmergencyApiService(EmergencyApiCaller apiCaller, 
                              HospitalMainApiRepository hospitalMainApiRepository,
                              RegionConfig regionConfig) { 
        this.emergencyApiCaller = apiCaller;
        this.objectMapper = new ObjectMapper();
        this.hospitalMainApiRepository = hospitalMainApiRepository;
        this.regionConfig = regionConfig; 
    }

    public void updateEmergencyRoomData() {
        if (!schedulerRunning.get()) return;

        try {
            System.out.println("응급실 데이터 업데이트 시작...");
            List<EmergencyResponse> list = getEmergencyRoomDataAsDto();

            if (!list.isEmpty()) {
                String jsonString = objectMapper.writeValueAsString(list);
                webSocketHandler.broadcastEmergencyRoomData(jsonString);
                System.out.println("응급실 DTO 데이터 WebSocket 브로드캐스트 완료");
            } else {
                System.out.println("API 응답 데이터가 비어있습니다.");
            }
        } catch (Exception e) {
            System.err.println("응급실 데이터 업데이트 중 오류 발생:");
            e.printStackTrace();
        }
    }

    public List<EmergencyResponse> getEmergencyRoomDataAsDto() {
        // regionConfig에서 지역명 가져오기
        JsonNode data = emergencyApiCaller.callEmergencyApiAsJsonNode(
                regionConfig.getEmergencyCityName(), 1, 10);

        System.out.println("=== 디버깅 시작 ===");
        System.out.println("1. 전체 응답: " + data);

        if (data == null) {
            System.out.println("❌ data가 null입니다.");
            return Collections.emptyList();
        }

        // data 노드가 아니라 바로 body 노드를 확인
        System.out.println("2. body 노드 존재: " + data.has("body"));
        if (!data.has("body")) {
            System.out.println("❌ 'body' 필드가 없습니다.");
            return Collections.emptyList();
        }

        JsonNode bodyNode = data.get("body");
        System.out.println("3. items 노드 존재: " + bodyNode.has("items"));
        if (!bodyNode.has("items")) {
            System.out.println("❌ 'items' 필드가 없습니다.");
            return Collections.emptyList();
        }

        JsonNode itemsNode = bodyNode.get("items");
        System.out.println("4. item 노드 존재: " + itemsNode.has("item"));
        if (!itemsNode.has("item")) {
            System.out.println("❌ 'item' 필드가 없습니다.");
            return Collections.emptyList();
        }

        JsonNode itemArray = itemsNode.get("item");
        System.out.println("5. item 배열 타입: " + itemArray.getNodeType());
        System.out.println("6. item 배열 크기: " + (itemArray.isArray() ? itemArray.size() : "배열이 아님"));

        try {
            EmergencyResponse[] responses = objectMapper.treeToValue(itemArray, EmergencyResponse[].class);
            System.out.println("7. ✅ 변환 성공! 개수: " + responses.length);

            // 각 응급실 정보에 좌표 추가
            List<EmergencyResponse> responseList = Arrays.asList(responses);
            for (EmergencyResponse response : responseList) {
                System.out.println("   - " + response.getDutyName() + " 처리 중...");

                // 부분 매칭으로 검색
                List<HospitalMain> hospitals = hospitalMainApiRepository
                        .findByHospitalNameContaining(response.getDutyName());

                if (!hospitals.isEmpty()) {
                    HospitalMain hospitalData = hospitals.get(0); // 첫 번째 결과 사용
                    response.setCoordinates(hospitalData.getCoordinateX(), hospitalData.getCoordinateY());
                    response.setEmergencyAddress(hospitalData.getHospitalAddress());
                    System.out.println(
                            "     좌표: (" + hospitalData.getCoordinateX() + ", " + hospitalData.getCoordinateY() + ")");
                    System.out.println("     주소: " + hospitalData.getHospitalAddress());
                } else {
                    System.out.println("     좌표 및 주소 정보 없음");
                    response.setCoordinates(null, null);
                    response.setEmergencyAddress(null);
                }
            }

            return responseList;
        } catch (Exception e) {
            System.err.println("JSON 변환 중 오류:");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void startScheduler() {
        schedulerRunning.set(true);
        scheduledTask = taskScheduler.scheduleAtFixedRate(() -> {
            updateEmergencyRoomData();
        }, Duration.ofSeconds(30));
        System.out.println("30초마다 실행하는 스케줄러 시작!");
    }

    public void stopScheduler() {
        schedulerRunning.set(false);
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(true);
        }
    }

    public JsonNode getEmergencyRoomData() {
        return emergencyApiCaller.callEmergencyApiAsJsonNode(
                regionConfig.getEmergencyCityName(), 1, 10);
    }

    public void shutdownCompleteService() {
        stopScheduler();
        webSocketHandler.closeAllSessions();
        System.out.println("응급실 서비스 완전 종료 완료");
    }
}
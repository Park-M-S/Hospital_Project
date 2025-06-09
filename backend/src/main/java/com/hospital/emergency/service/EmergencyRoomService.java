package com.hospital.emergency.service;

import com.hospital.emergency.client.EmergencyRoomApiCaller;
import com.hospital.emergency.dto.EmergencyRoomApiItem;
import com.hospital.emergency.dto.EmergencyRoomApiResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class EmergencyRoomService {

    private final EmergencyRoomApiCaller emergencyRoomApiCaller;

    public EmergencyRoomService(EmergencyRoomApiCaller emergencyRoomApiCaller) {
        this.emergencyRoomApiCaller = emergencyRoomApiCaller;
    }

    public List<EmergencyRoomApiItem> getEmergencyRoomsByLocation(double latitude, double longitude) {
        EmergencyRoomApiResponse apiResponse = emergencyRoomApiCaller.callEmergencyRoomApi(latitude, longitude);

        // API 응답이 유효하고, 응답 본문에 아이템들이 존재하는지 확인합니다.
        // DTO 구조: EmergencyRoomApiResponse -> body -> items -> item (List)
        // 'response' 필드가 없으므로, apiResponse에서 바로 getBody()를 호출합니다.
        if (apiResponse != null && apiResponse.getBody() != null &&
            apiResponse.getBody().getItems() != null) { // <-- getResponse() 대신 getBody() 사용

            List<EmergencyRoomApiItem> items = apiResponse.getBody().getItems().getItem();

            if (items != null && !items.isEmpty()) {
                System.out.println("Service: Successfully retrieved " + items.size() + " emergency room(s).");
                return items;
            }
        }

        System.out.println("Service: No emergency rooms found or API response was invalid/empty.");
        return Collections.emptyList();
    }
}
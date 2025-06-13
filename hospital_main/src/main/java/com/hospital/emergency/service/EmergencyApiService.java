package com.hospital.emergency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.hospital.dto.web.EmergencyResponse;
import com.hospital.emergency.parser.EmergencyApiParser;
import com.hospital.entity.HospitalMain;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.websocket.EmergencyApiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EmergencyApiService {

	private final AtomicBoolean schedulerRunning = new AtomicBoolean(false);
	private final ObjectMapper jsonMapper;
	private final EmergencyApiParser apiParser;
	private final HospitalMainApiRepository hospitalMainApiRepository;

	@Autowired
	private EmergencyApiWebSocketHandler webSocketHandler;

	public EmergencyApiService(EmergencyApiParser apiParser, HospitalMainApiRepository hospitalMainApiRepository) {
		this.apiParser = apiParser;
		this.jsonMapper = new ObjectMapper();
		this.hospitalMainApiRepository = hospitalMainApiRepository;
	}

	@Scheduled(fixedRate = 30000)
	public void updateEmergencyRoomData() {
		if (!schedulerRunning.get())
			return;

		try {
			System.out.println("응급실 데이터 업데이트 시작...");
			List<EmergencyResponse> list = getEmergencyRoomDataAsDto();

			if (!list.isEmpty()) {
				String jsonString = jsonMapper.writeValueAsString(list);
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
		JsonNode data = apiParser.callEmergencyApiAsJsonNode("성남시", 1, 10);

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
			EmergencyResponse[] responses = jsonMapper.treeToValue(itemArray, EmergencyResponse[].class);
			System.out.println("7. ✅ 변환 성공! 개수: " + responses.length);

			// 각 응급실 정보에 좌표 추가
			List<EmergencyResponse> responseList = Arrays.asList(responses);
			for (EmergencyResponse response : responseList) {
			    System.out.println("   - " + response.getDutyName() + " 처리 중...");

			    // 부분 매칭으로 검색
			    List<HospitalMain> hospitals = hospitalMainApiRepository.findByHospitalNameContaining(response.getDutyName());
			    
			    if (!hospitals.isEmpty()) {
			        HospitalMain hospitalData = hospitals.get(0); // 첫 번째 결과 사용
			        response.setCoordinates(hospitalData.getCoordinateX(), hospitalData.getCoordinateY());
			        System.out.println("     좌표: (" + hospitalData.getCoordinateX() + ", " + hospitalData.getCoordinateY() + ")");
			    } else {
			        System.out.println("     좌표 정보 없음");
			        response.setCoordinates(null, null);
			    }
			}

			return responseList;
		} catch (Exception e) {
			System.err.println("❌ JSON 변환 중 오류:");
			e.printStackTrace();
			return Collections.emptyList();
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

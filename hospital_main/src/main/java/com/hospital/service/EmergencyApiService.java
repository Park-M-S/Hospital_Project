package com.hospital.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.caller.EmergencyApiCaller;
import com.hospital.config.RegionConfig;
import com.hospital.dto.api.EmergencyWebResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.websocket.EmergencyApiWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EmergencyApiService {

	private final AtomicBoolean schedulerRunning = new AtomicBoolean(false);
	private final ObjectMapper objectMapper;
	private final EmergencyApiCaller emergencyApiCaller;
	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final RegionConfig regionConfig;

	private final EmergencyApiWebSocketHandler webSocketHandler;
	private final TaskScheduler taskScheduler;

	private ScheduledFuture<?> apiUpdateTask;
	private ScheduledFuture<?> broadcastTask;

	private volatile String latestEmergencyJson = null;

	@Autowired
	public EmergencyApiService(EmergencyApiCaller apiCaller, HospitalMainApiRepository hospitalMainApiRepository,
			RegionConfig regionConfig, EmergencyApiWebSocketHandler webSocketHandler, TaskScheduler taskScheduler) {
		this.emergencyApiCaller = apiCaller;
		this.objectMapper = new ObjectMapper();
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.regionConfig = regionConfig;
		this.webSocketHandler = webSocketHandler;
		this.taskScheduler = taskScheduler;
	}

	public void startScheduler() {
		if (schedulerRunning.get())
			return;
		schedulerRunning.set(true);

		try {
			List<EmergencyWebResponse> list = getEmergencyRoomDataAsDto();
			if (!list.isEmpty()) {
				latestEmergencyJson = objectMapper.writeValueAsString(list);
				System.out.println("✅ 응급실 초기 데이터 업데이트 성공");
			}
		} catch (Exception e) {
			System.err.println("❌ 초기 데이터 업데이트 중 오류:");
			e.printStackTrace();
		}

		// ✅ 1. API 호출은 30초마다
		apiUpdateTask = taskScheduler.scheduleAtFixedRate(() -> {
			try {
				List<EmergencyWebResponse> list = getEmergencyRoomDataAsDto();
				if (!list.isEmpty()) {
					latestEmergencyJson = objectMapper.writeValueAsString(list);
					System.out.println("✅ 응급실 데이터 업데이트 성공");
				}
			} catch (Exception e) {
				System.err.println("❌ 응급실 데이터 업데이트 중 오류:");
				e.printStackTrace();
			}
		}, Duration.ofSeconds(30));

		// ✅ 2. WebSocket 브로드캐스트는 1초마다
		broadcastTask = taskScheduler.scheduleAtFixedRate(() -> {
			if (latestEmergencyJson != null) {
				webSocketHandler.broadcastEmergencyRoomData(latestEmergencyJson);
			}
		}, Duration.ofSeconds(1));

		System.out.println("✅ 스케줄러 시작: API 30초, 브로드캐스트 1초");
	}

	public void stopScheduler() {
		schedulerRunning.set(false);
		if (apiUpdateTask != null && !apiUpdateTask.isCancelled()) {
			apiUpdateTask.cancel(true);
		}
		if (broadcastTask != null && !broadcastTask.isCancelled()) {
			broadcastTask.cancel(true);
		}
		System.out.println("🛑 스케줄러 정지 완료");
	}

	public void shutdownCompleteService() {
		stopScheduler();
		webSocketHandler.closeAllSessions();
		System.out.println("🚫 응급실 서비스 완전 종료 완료");
	}

	public List<EmergencyWebResponse> getEmergencyRoomDataAsDto() {
		JsonNode data = emergencyApiCaller.callEmergencyApiAsJsonNode(regionConfig.getEmergencyCityName(), 1, 10);

		if (data == null || !data.has("body"))
			return Collections.emptyList();

		JsonNode itemsNode = data.get("body").get("items");
		if (itemsNode == null || !itemsNode.has("item"))
			return Collections.emptyList();

		try {
			JsonNode itemArray = itemsNode.get("item");
			EmergencyWebResponse[] responses = objectMapper.treeToValue(itemArray, EmergencyWebResponse[].class);
			List<EmergencyWebResponse> responseList = Arrays.asList(responses);

			for (EmergencyWebResponse response : responseList) {
				List<HospitalMain> hospitals = hospitalMainApiRepository
						.findByHospitalNameContaining(response.getDutyName());

				if (!hospitals.isEmpty()) {
					HospitalMain hospitalData = hospitals.get(0);
					response.setCoordinates(hospitalData.getCoordinateX(), hospitalData.getCoordinateY());
					response.setEmergencyAddress(hospitalData.getHospitalAddress());
				} else {
					response.setCoordinates(null, null);
					response.setEmergencyAddress(null);
				}
			}

			return responseList;
		} catch (Exception e) {
			System.err.println("❌ JSON 변환 중 오류:");
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	// 초기 연결 시 데이터 제공용 (1회)
	public JsonNode getEmergencyRoomData() {
		return emergencyApiCaller.callEmergencyApiAsJsonNode(regionConfig.getEmergencyCityName(), 1, 10);
	}
}

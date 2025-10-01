package com.hospital.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.EmergencyApiResponse;
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
import java.util.stream.Collectors;

@Service
public class EmergencyMockService {

	private final EmergencyMockDataGenerator mockDataGenerator;
	private final ObjectMapper objectMapper;
	private volatile String latestEmergencyJson = null;
	private final AtomicBoolean schedulerRunning = new AtomicBoolean(false);

	@Autowired
	public EmergencyMockService(EmergencyMockDataGenerator mockDataGenerator) {
		this.mockDataGenerator = mockDataGenerator;
		this.objectMapper = new ObjectMapper();
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
	 * Mock WebSocket 연결 시 호출 - 스케줄러 상태만 활성화
	 */
	public void onMockWebSocketConnected() {
		if (schedulerRunning.compareAndSet(false, true)) {
			System.out.println("🔧 Mock 모드 활성화 - Mock 데이터 사용");

			// MockDataGenerator의 스케줄러만 활성화 (자체 스케줄러는 시작 안 함)
			mockDataGenerator.enableScheduler();

			// 초기 데이터 로드
			updateMockDataCache();
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

			if (!newJsonData.equals(latestEmergencyJson)) {
				latestEmergencyJson = newJsonData;
				// 브로드캐스트 코드 제거 - WebSocketHandler에서 처리
				System.out.println("✅ Mock 응급실 데이터 업데이트 완료");
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
	 * Mock 스케줄러 상태 확인
	 */
	public boolean isMockSchedulerRunning() {
		return schedulerRunning.get();
	}

	public void forceUpdateData() {
		updateMockDataCache();
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
package com.hospital.emergency.controller;

import com.hospital.emergency.dto.EmergencyRoomApiItem; // 서비스에서 반환할 DTO 임포트
import com.hospital.emergency.service.EmergencyRoomService; // 서비스 계층 임포트
import org.springframework.http.ResponseEntity; // HTTP 응답을 위한 클래스
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; // RESTful API 컨트롤러임을 명시

import java.util.Collections;
import java.util.List;

@RestController // 이 클래스가 RESTful API를 처리하는 컨트롤러임을 나타냅니다.
@RequestMapping("/api/emergency-rooms") // 이 컨트롤러의 모든 핸들러 메서드에 대한 기본 URL 경로를 설정합니다.
public class EmergencyRoomController {

	private final EmergencyRoomService emergencyRoomService; // 서비스 계층을 주입받습니다.

	// 생성자 주입 (Spring이 자동으로 EmergencyRoomService 빈을 찾아 주입해 줍니다.)
	public EmergencyRoomController(EmergencyRoomService emergencyRoomService) {
		this.emergencyRoomService = emergencyRoomService;
	}

	@GetMapping("/nearby") // GET 요청을 처리하고, /nearby 경로에 매핑됩니다.
	public ResponseEntity<List<EmergencyRoomApiItem>> getNearbyEmergencyRooms(@RequestParam double latitude, @RequestParam double longitude) {

		System.out.println("Controller: Received request for nearby emergency rooms at Latitude: " + latitude
				+ ", Longitude: " + longitude);

		// 서비스 계층의 메서드를 호출하여 응급실 정보를 가져옵니다.
		List<EmergencyRoomApiItem> emergencyRooms = emergencyRoomService.getEmergencyRoomsByLocation(latitude,
				longitude);

		if (emergencyRooms.isEmpty()) {
			System.out.println("Controller: No emergency rooms found for the given location.");
			// 응급실이 없으면 204 No Content 또는 404 Not Found를 반환할 수 있습니다.
			// 여기서는 200 OK와 빈 리스트를 반환하여 클라이언트가 쉽게 처리하도록 합니다.
			return ResponseEntity.ok(Collections.emptyList()); // import java.util.Collections;
		} else {
			System.out.println("Controller: Returning " + emergencyRooms.size() + " emergency room(s).");
			// 응급실 정보가 있으면 200 OK와 함께 리스트를 반환합니다.
			return ResponseEntity.ok(emergencyRooms);
		}
	}

	// 필요하다면 여기에 다른 API 엔드포인트를 추가할 수 있습니다.
	// 예: 특정 응급실 상세 정보 조회, 응급실 이름으로 검색 등
}
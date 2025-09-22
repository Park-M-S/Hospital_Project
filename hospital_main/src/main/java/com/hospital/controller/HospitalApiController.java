package com.hospital.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.service.HospitalDetailApiService;
import com.hospital.service.HospitalMainApiService;
import com.hospital.service.MedicalSubjectApiService;
import com.hospital.service.PharmacyApiService;
import com.hospital.service.ProDocApiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class HospitalApiController {
	
	@Value("${api.admin.key}")
	private String adminApiKey;

	private final HospitalMainApiService hospitalMainService;
	private final HospitalDetailApiService hospitalDetailApiService;
	private final MedicalSubjectApiService medicalSubjectApiService;
	private final ProDocApiService proDocApiService;
	private final PharmacyApiService pharmacyApiService;

	public HospitalApiController(HospitalMainApiService hospitalMainService,
			HospitalDetailApiService hospitalDetailApiService, MedicalSubjectApiService medicalSubjectApiService,
			ProDocApiService proDocApiService, PharmacyApiService pharmacyApiService) {
		this.hospitalMainService = hospitalMainService;
		this.hospitalDetailApiService = hospitalDetailApiService;
		this.medicalSubjectApiService = medicalSubjectApiService;
		this.proDocApiService = proDocApiService;
		this.pharmacyApiService = pharmacyApiService;
	}
	
	private boolean isValidApiKey(String apiKey) {
		if (apiKey == null || apiKey.trim().isEmpty()) {
			return false;
		}
		return adminApiKey.equals(apiKey);
	}
	
	private ResponseEntity<Map<String, Object>> unauthorizedResponse() {
		Map<String, Object> response = new HashMap<>();
		response.put("success", false);
		response.put("error", "UNAUTHORIZED");
		response.put("message", "유효하지 않은 API 키입니다");
		response.put("timestamp", LocalDateTime.now());
		
		log.warn("API 키 인증 실패 - IP: {}", getClientIp());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}
	
	private String getClientIp() {
	
		return "unknown";
	}

	//병원 기본 정보를 DB에 저장 - JSON 응답으로 변경
	@PostMapping(value = "/main/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> updateHospitalMain(
			@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
		
		// API 키 검증
		if (!isValidApiKey(apiKey)) {
			return unauthorizedResponse();
		}

		log.info("병원 기본 정보 저장 시작... (인증된 요청)");
		
		int savedCount = hospitalMainService.updateHospitalMain();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "병원 정보 저장 완료");
		response.put("savedCount", savedCount);
		response.put("timestamp", LocalDateTime.now());
		
		log.info("병원 정보 {}개 DB 저장 완료!", savedCount);
		return ResponseEntity.ok(response);
	}
	@GetMapping(value = "/main/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getMainStatus() {
	    int done = hospitalMainService.getCompletedCount();
	    int fail = hospitalMainService.getFailedCount();
	    
	    Map<String, Object> response = new HashMap<>();
	    response.put("success", true);
	    response.put("completed", done);
	    response.put("failed", fail);
	    response.put("total", done + fail);
	    response.put("timestamp", LocalDateTime.now());
	    
	    return ResponseEntity.ok(response);
	}


	//병원 상세 정보 수집 시작 - JSON 응답으로 변경
	@PostMapping(value = "/details/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> updateHospitalDetails(
			@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
		
		// API 키 검증
		if (!isValidApiKey(apiKey)) {
			return unauthorizedResponse();
		}

		log.info("병원 상세정보 저장 시작... (인증된 요청)");
		
		int total = hospitalDetailApiService.updateHospitalDetails();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "병원 상세정보 저장 시작됨");
		response.put("totalCount", total);
		response.put("note", "실시간 진행상황은 로그에서 확인 가능");
		response.put("timestamp", LocalDateTime.now());
		
		log.info("병원 상세정보 저장 시작됨! 전체 병원 수: {}개", total);
		return ResponseEntity.ok(response);
	}

	//병원 상세 정보 수집 진행 상황 조회 - JSON 응답으로 변경
	@GetMapping(value = "/details/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getDetailsStatus() {
		int done = hospitalDetailApiService.getCompletedCount();
		int fail = hospitalDetailApiService.getFailedCount();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("completed", done);
		response.put("failed", fail);
		response.put("total", done + fail);
		response.put("timestamp", LocalDateTime.now());
		
		return ResponseEntity.ok(response);
	}

	//진료과목 저장 - JSON 응답으로 변경
	@PostMapping(value = "/subject/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> updateSubjects(
			@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
		
		// API 키 검증
		if (!isValidApiKey(apiKey)) {
			return unauthorizedResponse();
		}

		log.info("진료과목 저장 시작... (인증된 요청)");
		
		int total = medicalSubjectApiService.updateSubjects();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "진료과목 저장 시작됨");
		response.put("totalCount", total);
		response.put("note", "진행상황은 로그 또는 /status API로 확인");
		response.put("timestamp", LocalDateTime.now());
		
		log.info("진료과목 저장 시작됨! 전체 병원 수: {}개", total);
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = "/subject/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getSubjectStatus() {
		int done = medicalSubjectApiService.getCompletedCount();
		int fail = medicalSubjectApiService.getFailedCount();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("completed", done);
		response.put("failed", fail);
		response.put("total", done + fail);
		response.put("timestamp", LocalDateTime.now());
		
		return ResponseEntity.ok(response);
	}

	//전문의 정보 저장 - JSON 응답으로 변경
	@PostMapping(value = "/proDoc/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> updateProDocs(
			@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
		
		// API 키 검증
		if (!isValidApiKey(apiKey)) {
			return unauthorizedResponse();
		}

		log.info("전문의 정보 저장 시작... (인증된 요청)");
		
		int total = proDocApiService.updateProDoc();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "전문의 정보 저장 완료");
		response.put("totalCount", total);
		response.put("timestamp", LocalDateTime.now());
		
		log.info("전문의 정보 저장 완료! 전체 병원 수: {}개", total);
		return ResponseEntity.ok(response);
	}

	@GetMapping(value = "/proDoc/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getProDocStatus() {
		int done = proDocApiService.getCompletedCount();
		int fail = proDocApiService.getFailedCount();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("completed", done);
		response.put("failed", fail);
		response.put("total", done + fail);
		response.put("timestamp", LocalDateTime.now());
		
		return ResponseEntity.ok(response);
	}

	//약국 데이터 저장 - JSON 응답으로 변경
	@PostMapping(value = "/pharmacy/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> updatePharmacy(
			@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
		
		// API 키 검증
		if (!isValidApiKey(apiKey)) {
			return unauthorizedResponse();
		}

		log.info("약국 데이터 저장 시작... (인증된 요청)");
		
		int totalSaved = pharmacyApiService.fetchAndSaveSeongnamPharmacies();
		
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "약국 데이터 저장 완료");
		response.put("savedCount", totalSaved);
		response.put("area", "성남시 전체");
		response.put("timestamp", LocalDateTime.now());
		
		log.info("약국 데이터 저장 완료! 총 {}건 저장됨", totalSaved);
		return ResponseEntity.ok(response);
	}

}
package com.hospital.controller;


import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

	// 병원 기본 정보를 DB에 저장
	@PostMapping(value = "/main/save", produces = MediaType.TEXT_PLAIN_VALUE)
	public String saveHospitalsToDb() {
		int savedCount = 0;
		try {
			log.info("병원 기본 정보 저장 시작...");
			savedCount = hospitalMainService.fetchParseAndSaveHospitals();
			String result = "병원 정보 " + savedCount + "개 DB 저장 완료!";
			log.info(result);
			return result;
		} catch (Exception e) {
			log.error("병원 정보 DB 저장 중 오류 발생", e);
			return "병원 정보 DB 저장 중 오류 발생: " + e.getMessage();
		}
	}

	// 병원 상세 정보 수집 시작 (비동기 처리)
	@PostMapping(value = "/details/save", produces = MediaType.TEXT_PLAIN_VALUE)
	public String updateHospitalDetails() {
		
			int total = hospitalDetailApiService.updateAllHospitalDetails(); // 전체 병원 수 반환
			return String.format("병원 상세정보 저장 시작됨! 전체 병원 수: %d개\n(실시간 진행상황은 로그에서 확인 가능)", total);
			
	}
	

	// 병원 상세 정보 수집 진행 상황 조회
	@GetMapping(value = "/details/status", produces = MediaType.TEXT_PLAIN_VALUE)
	public String getUpdateStatus() {

		int done = hospitalDetailApiService.getCompletedCount();
		int fail = hospitalDetailApiService.getFailedCount();

		return String.format("현재 진행상황: 완료 %d건, 실패 %d건\n", done, fail);
	}

	@PostMapping(value = "/subject/save", produces = MediaType.TEXT_PLAIN_VALUE)
	public String saveSubjects() {
		int total = medicalSubjectApiService.fetchParseAndSaveMedicalSubjects(); // 전체 병원 수 반환
		return String.format("진료과목 저장 시작됨! 전체 병원 수: %d개\n(진행상황은 로그 또는 /status API로 확인)\n", total);
	}

	@GetMapping(value = "/subject/status", produces = MediaType.TEXT_PLAIN_VALUE)
	public String getMedicalStatus() {
		int done = medicalSubjectApiService.getCompletedCount(); // 완료된 병원 수
		int fail = medicalSubjectApiService.getFailedCount(); // 실패한 병원 수
		return String.format("진료과목 진행상황: 완료 %d건, 실패 %d건\n", done, fail);
	}

	@PostMapping(value = "/proDoc/save", produces = MediaType.TEXT_PLAIN_VALUE)
	public String syncProDocData() {
		int total = proDocApiService.fetchParseAndSaveProDocs(); // 전체 병원 수 반환
		return String.format("전문의 정보 저장 와료! 전체 병원 수: %d개", total);
	}

	// 전문의 정보 저장 진행상황 확인 API
	// 병원 단위로 완료/실패 카운트 반환
	@GetMapping(value = "/proDoc/status", produces = MediaType.TEXT_PLAIN_VALUE)
	public String getProDocStatus() {
		int done = proDocApiService.getCompletedCount(); // 저장 완료된 병원 수
		int fail = proDocApiService.getFailedCount(); // 실패한 병원 수
		return String.format("현재 진행상황: 완료 %d건, 실패 %d건\n", done, fail);
	}

	@PostMapping(value = "/pharmacy/save", produces = "text/plain;charset=UTF-8")
	public String savePharmacyData() {
		int totalSaved = pharmacyApiService.fetchAndSaveSeongnamPharmacies();

		return String.format("약국 데이터 저장 완료! 총 %d건 저장됨 (성남시 전체)", totalSaved);
	}
}
package com.hospital.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.dto.HospitalWebResponse;
import com.hospital.dto.PharmacyWebResponse;
import com.hospital.service.HospitalWebService;
import com.hospital.service.PharmacyWebService;

//병원,약국 조회
@RestController
@RequestMapping("/web")
public class HospitalController {

	private static final Logger log = LoggerFactory.getLogger(HospitalController.class);

	private final HospitalWebService hospitalService;
	private final PharmacyWebService pharmacyService;

	@Autowired
	public HospitalController(HospitalWebService hospitalService, PharmacyWebService pharmacyService) {
		this.hospitalService = hospitalService;
		this.pharmacyService = pharmacyService;
	}

	// 병원 위치기반 데이터
	  @GetMapping(value = "/hospitalsData", produces = MediaType.APPLICATION_JSON_VALUE)
	    public List<HospitalWebResponse> getHospitals(
	            @RequestParam double userLat,         // 사용자 위도
	            @RequestParam double userLng,         // 사용자 경도
	            @RequestParam double radius          // 검색 반경 (km)

	    ) {
	        long startTime = System.currentTimeMillis();
	        log.info("[NEW 방식] 병원 검색 API 호출 - 위도: {}, 경도: {}, 반경: {}km", userLat, userLng, radius);

	        List<HospitalWebResponse> result = hospitalService.getOptimizedHospitalsV2(userLat, userLng, radius);

	        long endTime = System.currentTimeMillis();
	        log.info("[NEW 방식] 병원 검색 완료 - 조회된 병원 수: {}개, 응답 시간: {}ms", result.size(), (endTime - startTime));

	        return result;
	    }

	// 약국 검색 API
	@GetMapping(value = "/pharmaciesData", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PharmacyWebResponse> getNearbyPharmacies(@RequestParam("userLat") double userLat,
			@RequestParam("userLng") double userLng, @RequestParam("radius") double radius) {
		log.info("약국 검색 API 호출 - 위도: {}, 경도: {}, 반경: {}km", userLat, userLng, radius);

		List<PharmacyWebResponse> result = pharmacyService.getPharmacies(userLat, userLng, radius);

		log.info("약국 검색 완료 - 조회된 약국 수: {}개", result.size());

		return result;
	}

	// 성능 비교용 - ST_Distance_Sphere만 사용 (인덱스 미사용)
	@GetMapping(value = "/hospitalsData/old", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<HospitalWebResponse> getHospitalsOld(
			@RequestParam double userLat,
			@RequestParam double userLng,
			@RequestParam double radius
	) {
		long startTime = System.currentTimeMillis();
		log.info("[OLD 방식] 병원 검색 API 호출 - 위도: {}, 경도: {}, 반경: {}km", userLat, userLng, radius);

		List<HospitalWebResponse> result = hospitalService.getHospitalsWithoutSpatialIndex(userLat, userLng, radius);

		long endTime = System.currentTimeMillis();
		log.info("[OLD 방식] 병원 검색 완료 - 조회된 병원 수: {}개, 응답 시간: {}ms", result.size(), (endTime - startTime));

		return result;
	}


}
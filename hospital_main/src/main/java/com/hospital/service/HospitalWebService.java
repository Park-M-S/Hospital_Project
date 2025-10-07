package com.hospital.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.config.RegionConfig;
import com.hospital.converter.HospitalConverter;
import com.hospital.dto.HospitalWebResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.util.DistanceCalculator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class HospitalWebService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final HospitalConverter hospitalConverter;
	private final DistanceCalculator distanceCalculator;

	private static final double KM_PER_DEGREE_LAT = 110.0;

	@Autowired
	public HospitalWebService(HospitalMainApiRepository hospitalMainApiRepository, HospitalConverter hospitalConverter,
			DistanceCalculator distanceCalculator, RegionConfig regionConfig) {
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.hospitalConverter = hospitalConverter;
		this.distanceCalculator = distanceCalculator;
	}

	/**
	 * ✅ 공간 인덱스 + 정확 거리 필터 (2단계 쿼리 구조)
	 */
	public List<HospitalWebResponse> getOptimizedHospitals(double userLat, double userLng, double radius) {
		long startTime = System.currentTimeMillis();
		log.info("=== Optimized Spatial Query Mode (MBR Manual) ===");
		log.info("User Location: lat={}, lng={}", userLat, userLng);
		log.info("Radius: {}km", radius);

		
		double deltaDegreeY = radius / KM_PER_DEGREE_LAT;

		double kmPerDegreeLon = 111.32 * Math.cos(Math.toRadians(userLat));
		double deltaDegreeX = radius / kmPerDegreeLon;

		log.debug("Calculated MBR Deltas: deltaX={}, deltaY={}", deltaDegreeX, deltaDegreeY);

	
		long step1Start = System.currentTimeMillis();


		List<String> hospitalCodes = hospitalMainApiRepository.findHospitalCodesBySpatialQuery(userLat, userLng,
				deltaDegreeX, deltaDegreeY, radius);

		log.info("Step1 (Spatial + Distance Filter): {}ms, filtered: {}", System.currentTimeMillis() - step1Start,
				hospitalCodes.size());

		if (hospitalCodes.isEmpty()) {
			log.info("No hospitals found within range.");
			log.info("Total: {}ms", System.currentTimeMillis() - startTime);
			return List.of();
		}

	
		long step2Start = System.currentTimeMillis();
		List<HospitalMain> hospitals = hospitalMainApiRepository.findByHospitalCodeIn(hospitalCodes);
		log.info("Step2 (Main Entity Fetch): {}ms, count: {}", System.currentTimeMillis() - step2Start,
				hospitals.size());

		long step3Start = System.currentTimeMillis();
		List<HospitalWebResponse> result = hospitals.stream().map(hospitalConverter::convertToDTO)
				.collect(Collectors.toList());
		log.info("Step3 (DTO Convert + Lazy Loading): {}ms", System.currentTimeMillis() - step3Start);

		log.info("Total (Optimized MBR Manual): {}ms", System.currentTimeMillis() - startTime);
		return result;
	}

	

	/**
	 * ✅ 인덱스 미사용 - ST_Distance_Sphere만 사용
	 */
	public List<HospitalWebResponse> getHospitalsWithDistanceOnly(double userLat, double userLng, double radius) {
		long startTime = System.currentTimeMillis();
		log.info("=== Distance Only Query Mode ===");
		log.info("User Location: lat={}, lng={}", userLat, userLng);
		log.info("Radius: {}km", radius);

		// Step 1: 거리 기반 필터 (PK만 조회)
		long step1Start = System.currentTimeMillis();
		List<String> hospitalCodes = hospitalMainApiRepository.findHospitalCodesByDistanceOnly(userLat, userLng,
				radius);
		log.info("Step1 (Distance Filter Only): {}ms, filtered: {}", System.currentTimeMillis() - step1Start,
				hospitalCodes.size());

		if (hospitalCodes.isEmpty()) {
			log.info("No hospitals found within range.");
			log.info("Total: {}ms", System.currentTimeMillis() - startTime);
			return List.of();
		}

		// Step 2: 코드 기반 엔티티 조회
		long step2Start = System.currentTimeMillis();
		List<HospitalMain> hospitals = hospitalMainApiRepository.findByHospitalCodeIn(hospitalCodes);
		log.info("Step2 (Main Entity Fetch): {}ms, count: {}", System.currentTimeMillis() - step2Start,
				hospitals.size());

		// Step 3: DTO 변환
		long step3Start = System.currentTimeMillis();
		List<HospitalWebResponse> result = hospitals.stream().map(hospitalConverter::convertToDTO)
				.collect(Collectors.toList());
		log.info("Step3 (DTO Convert + Lazy Loading): {}ms", System.currentTimeMillis() - step3Start);

		log.info("Total (Distance Only): {}ms", System.currentTimeMillis() - startTime);
		return result;
	}
}

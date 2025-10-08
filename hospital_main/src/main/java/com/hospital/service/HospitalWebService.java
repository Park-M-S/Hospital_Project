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
	    log.info("=== Optimized Spatial Query (Single Query) ===");
	    log.info("User Location: lat={}, lng={}", userLat, userLng);
	    log.info("Radius: {}km", radius);

	    // MBR 계산
	    double deltaDegreeY = radius / KM_PER_DEGREE_LAT;
	    double kmPerDegreeLon = 111.32 * Math.cos(Math.toRadians(userLat));
	    double deltaDegreeX = radius / kmPerDegreeLon;

	    double minLon = userLng - deltaDegreeX;
	    double maxLon = userLng + deltaDegreeX;
	    double minLat = userLat - deltaDegreeY;
	    double maxLat = userLat + deltaDegreeY;

	    log.debug("MBR: lon[{}, {}], lat[{}, {}]", minLon, maxLon, minLat, maxLat);

	    // 1. DB 쿼리 수행
	    long queryStart = System.nanoTime();
	    List<HospitalMain> hospitals = hospitalMainApiRepository.findByMBRDirect(minLon, maxLon, minLat, maxLat);
	    long queryMs = (System.nanoTime() - queryStart) / 1_000_000;
	    log.info("Step1: DB Query findByMBRDirect: {}ms, count={}", queryMs, hospitals.size());

	    if (hospitals.isEmpty()) {
	        log.info("No hospitals found. Total: {}ms", System.currentTimeMillis() - startTime);
	        return List.of();
	    }

	    // 2. 엔티티 필드 접근 체크 (Lazy loading 여부)
	    long accessStart = System.currentTimeMillis();
	    HospitalMain first = hospitals.get(0);
	    String name = first.getHospitalName();
	    log.info("Step2: First entity field access: {}ms, name={}", System.currentTimeMillis() - accessStart, name);

	    // 3. 연관 엔티티 batch fetch 시간 측정
	    long batchFetchStart = System.currentTimeMillis();
	    hospitals.forEach(h -> {
	        // 예: 연관 엔티티 접근 시 Lazy 로딩 발생 체크
	        if (h.getMedicalSubjects() != null) h.getMedicalSubjects().size();
	    });
	    log.info("Step3: Batch fetch related entities: {}ms", System.currentTimeMillis() - batchFetchStart);

	    // 4. DTO 변환
	    long dtoConvertStart = System.currentTimeMillis();
	    List<HospitalWebResponse> result = hospitals.stream()
	            .map(hospitalConverter::convertToDTO)
	            .collect(Collectors.toList());
	    log.info("Step4: DTO conversion: {}ms", System.currentTimeMillis() - dtoConvertStart);

	    log.info("Total elapsed: {}ms", System.currentTimeMillis() - startTime);
	    return result;
	}

	

	/**
	 * ✅ 인덱스 미사용 - ST_Distance_Sphere만 사용
	 */
	public List<HospitalWebResponse> getHospitalsWithoutSpatialIndex(double userLat, double userLng, double radiusKm) {
	    long startTime = System.currentTimeMillis();
	    log.info("=== Distance Only Query Mode (MBR, No Spatial Index) ===");
	    log.info("User Location: lat={}, lng={}", userLat, userLng);
	    log.info("Radius: {}km", radiusKm);

	    // Step 1: 사각형 범위 계산 (MBR)
	    double deltaLat = radiusKm / 111.0; // 1도 ≈ 111km
	    double deltaLon = radiusKm / (111.0 * Math.cos(Math.toRadians(userLat)));

	    double minLat = userLat - deltaLat;
	    double maxLat = userLat + deltaLat;
	    double minLon = userLng - deltaLon;
	    double maxLon = userLng + deltaLon;

	    log.debug("MBR: lon[{}, {}], lat[{}, {}]", minLon, maxLon, minLat, maxLat);

	    // Step 2: MBR 기반 엔티티 조회 (공간 인덱스 미사용)
	    long queryStart = System.nanoTime();
	    List<HospitalMain> hospitals = hospitalMainApiRepository.findByMBRDirectWithoutIndex(
	            minLon, maxLon, minLat, maxLat); // h.* 반환
	    long queryMs = (System.nanoTime() - queryStart) / 1_000_000;
	    log.info("findByMBRDirectWithoutIndex returned: {}ms, count: {}", queryMs, hospitals.size());

	    if (hospitals.isEmpty()) {
	        log.info("No hospitals found. Total: {}ms", System.currentTimeMillis() - startTime);
	        return List.of();
	    }

	    // 첫 번째 엔티티 필드 접근 (완전히 로드되었는지 확인)
	    long accessStart = System.currentTimeMillis();
	    HospitalMain first = hospitals.get(0);
	    String name = first.getHospitalName();
	    log.info("First entity field access: {}ms, name: {}", System.currentTimeMillis() - accessStart, name);

	    // Step 3: DTO 변환
	    long convertStart = System.currentTimeMillis();
	    List<HospitalWebResponse> result = hospitals.stream()
	            .map(hospitalConverter::convertToDTO)
	            .collect(Collectors.toList());
	    log.info("DTO Convert + Batch Fetch: {}ms", System.currentTimeMillis() - convertStart);

	    log.info("Total: {}ms", System.currentTimeMillis() - startTime);
	    return result;
	}
}

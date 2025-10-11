package com.hospital.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.config.RegionConfig;
import com.hospital.converter.HospitalConverter;
import com.hospital.dto.HospitalWebResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.repository.HospitalJdbcRepository;
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
	private final HospitalJdbcRepository hospitalJdbcRepository;

	private static final double KM_PER_DEGREE_LAT = 110.0;

	@Autowired
	public HospitalWebService(HospitalMainApiRepository hospitalMainApiRepository, HospitalConverter hospitalConverter,
			DistanceCalculator distanceCalculator, RegionConfig regionConfig, HospitalJdbcRepository hospitalJdbcRepository) {
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.hospitalConverter = hospitalConverter;
		this.distanceCalculator = distanceCalculator;
		this.hospitalJdbcRepository = hospitalJdbcRepository;
	}

	/**
	 * ✅ 공간 인덱스 + 정확 거리 필터 (2단계 쿼리 구조)
	 */
	public List<HospitalWebResponse> getOptimizedHospitalsV2(double userLat, double userLng, double radius) {
	    long startTime = System.currentTimeMillis();
	    log.info("=== JDBC Optimized Query ===");
	    
	    // MBR 계산
	    double deltaDegreeY = radius / KM_PER_DEGREE_LAT;
	    double kmPerDegreeLon = 111.32 * Math.cos(Math.toRadians(userLat));
	    double deltaDegreeX = radius / kmPerDegreeLon;

	    double minLon = userLng - deltaDegreeX;
	    double maxLon = userLng + deltaDegreeX;
	    double minLat = userLat - deltaDegreeY;
	    double maxLat = userLat + deltaDegreeY;
	    
	    // JDBC로 조회
	    List<HospitalWebResponse> result = hospitalJdbcRepository.findByMBRDirect(
	        minLon, maxLon, minLat, maxLat
	    );
	    
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

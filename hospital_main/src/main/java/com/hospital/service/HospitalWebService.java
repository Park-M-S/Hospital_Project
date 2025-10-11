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

	private final HospitalJdbcRepository hospitalJdbcRepository;

	private static final double KM_PER_DEGREE_LAT = 110.0;

	@Autowired
	public HospitalWebService(HospitalJdbcRepository hospitalJdbcRepository) {

		this.hospitalJdbcRepository = hospitalJdbcRepository;
	}

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
		List<HospitalWebResponse> result = hospitalJdbcRepository.findByMBRDirect(minLon, maxLon, minLat, maxLat);

		log.info("Total elapsed: {}ms", System.currentTimeMillis() - startTime);
		return result;
	}

}

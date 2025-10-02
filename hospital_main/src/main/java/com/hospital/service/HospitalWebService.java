package com.hospital.service;

import com.hospital.entity.HospitalMain;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.util.DistanceCalculator;

import com.hospital.config.RegionConfig;
import com.hospital.converter.HospitalConverter;
import com.hospital.dto.HospitalWebResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HospitalWebService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final HospitalConverter hospitalConverter;
	private final DistanceCalculator distanceCalculator;

	@Autowired
	public HospitalWebService(HospitalMainApiRepository hospitalMainApiRepository, HospitalConverter hospitalConverter,
			DistanceCalculator distanceCalculator, RegionConfig regionConfig) {

		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.hospitalConverter = hospitalConverter;
		this.distanceCalculator = distanceCalculator;

	}

	public List<HospitalWebResponse> getHospitals(double userLat, double userLng, double radius) {
	    double radiusMeters = radius * 1000;

	    double latDegree = radiusMeters / 111320.0;
	    double lonDegree = radiusMeters / (111320.0 * Math.cos(Math.toRadians(userLat)));

	    List<HospitalMain> hospitalEntities = hospitalMainApiRepository.findHospitalsWithinBoundingBox(
	        userLat - latDegree,        // minLat
	        userLat + latDegree,        // maxLat
	        userLng - lonDegree,        // minLon
	        userLng + lonDegree         // maxLon
	    );

	    return hospitalEntities.stream()
	        .filter(hospital -> {
	            double distance = distanceCalculator.calculateDistance(
	                userLat, userLng,
	                hospital.getCoordinateY(), hospital.getCoordinateX()
	            );
	            return distance <= radiusMeters;
	        })
	        .map(hospitalConverter::convertToDTO)
	        .collect(Collectors.toList());
	}

	// 성능 비교용 - ST_Distance_Sphere만 사용 (인덱스 미사용)
	public List<HospitalWebResponse> getHospitalsWithDistanceOnly(double userLat, double userLng, double radius) {
	    List<HospitalMain> hospitalEntities = hospitalMainApiRepository.findHospitalsWithinRadius(
	        userLat,
	        userLng,
	        radius
	    );

	    return hospitalEntities.stream()
	        .map(hospitalConverter::convertToDTO)
	        .collect(Collectors.toList());
	}

}
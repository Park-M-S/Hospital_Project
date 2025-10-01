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
		List<HospitalMain> hospitalEntities = hospitalMainApiRepository.findHospitalsWithinRadius(userLat, userLng,
				radius);

		return hospitalEntities.stream() // stream() → parallelStream()
				.map(hospitalConverter::convertToDTO).collect(Collectors.toList());
	}

	// ✅ 병원명 검색
	@Cacheable(value = "hospitalsByName", key = "#hospitalName")
	public List<HospitalWebResponse> searchHospitalsByName(String hospitalName) {
		// 입력값 전처리
		String cleanInput = hospitalName.replace(" ", "");

		// Repository에서 검색 (hospitalDetail + medicalSubjects EAGER FETCH)
		List<HospitalMain> hospitalEntities = hospitalMainApiRepository.findHospitalsByName(cleanInput);

		// 단순히 DTO로 변환해서 리턴
		return hospitalEntities.stream().map(hospitalConverter::convertToDTO).collect(Collectors.toList());
	}

}
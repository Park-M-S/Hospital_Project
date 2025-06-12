package com.hospital.service;

import com.hospital.entity.HospitalMain;

import com.hospital.repository.HospitalRepository;
import com.hospital.util.DistanceCalculator;
import com.hospital.converter.HospitalConverter;
import com.hospital.domainLogic.HospitalTagFilter;

import com.hospital.dto.web.HospitalResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HospitalServiceImpl implements HospitalService {

	private final HospitalRepository hospitalRepository;
	private final HospitalFilter hospitalFilter;
	private final HospitalConverter hospitalConverter;
	private final DistanceCalculator  distanceCalculator;

	@Autowired
	public HospitalServiceImpl(HospitalRepository HospitalRepository, HospitalFilter hospitalFilter,
			HospitalConverter hospitalConverter,
			DistanceCalculator  distanceCalculator) {
		this.hospitalRepository = HospitalRepository;
		this.hospitalFilter = hospitalFilter;
		this.hospitalConverter = hospitalConverter;
		this.distanceCalculator = distanceCalculator;

	}

	@Override
	public List<HospitalResponseDTO> getHospitals(List<String> subs, double userLat, double userLng, double radius, List<String> tags) {
	    List<HospitalMain> hospitalEntities = hospitalRepository.findHospitalsBySubjects(subs);
	    
	    return hospitalEntities.stream()
	            .filter(hospital -> HospitalTagFilter.matchesAllTags(hospital, tags))
	            .map(hospital -> {
	                HospitalResponseDTO dto = hospitalConverter.convertToDTO(hospital);
	                double distance = distanceCalculator.calculateDistance(
	                        userLat, userLng, dto.getCoordinateY(), dto.getCoordinateX());
	                return new HospitalWithDistance(dto, distance);
	            })
	            .filter(hwd -> hwd.distance <= radius) // 반경 필터링
	            .sorted((h1, h2) -> Double.compare(h1.distance, h2.distance)) // 거리순 정렬
	            .map(hwd -> hwd.hospital) // 다시 HospitalResponseDTO만 추출
	            .collect(Collectors.toList());
	}

	// 거리와 함께 임시로 저장하는 내부 클래스
	private static class HospitalWithDistance {
	    final HospitalResponseDTO hospital;
	    final double distance;
	    
	    HospitalWithDistance(HospitalResponseDTO hospital, double distance) {
	        this.hospital = hospital;
	        this.distance = distance;
	    }
	}

}
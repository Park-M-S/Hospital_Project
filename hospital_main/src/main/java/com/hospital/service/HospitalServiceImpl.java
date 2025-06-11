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
	public List<HospitalResponseDTO> getHospitals(List<String> subs, double userLat, double userLng, double radius,
			List<String> tags) {

		List<HospitalMain> hospitalEntities = hospitalRepository.findHospitalsBySubjects(subs);
		
		List<HospitalResponseDTO> hospitals = hospitalEntities.stream()

				.filter(hospital -> HospitalTagFilter.matchesAllTags(hospital, tags))

				.map(this.hospitalConverter::convertToDTO)

				.filter(hospitalResponseDTO -> hospitalFilter.filterByDistance(hospitalResponseDTO, userLat, userLng,
						radius))
				.sorted((h1, h2) -> {
				    double distance1 = distanceCalculator.calculateDistance(
				        userLat, userLng, h1.getCoordinateY(), h1.getCoordinateX());
				    double distance2 = distanceCalculator.calculateDistance(
				        userLat, userLng, h2.getCoordinateY(), h2.getCoordinateX());
				    return Double.compare(distance1, distance2); // 가까운 순
				})

				.collect(Collectors.toList());

		return hospitals;
	}

}
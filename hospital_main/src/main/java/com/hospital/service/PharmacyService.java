package com.hospital.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.hospital.converter.PharmacyConverter;
import com.hospital.dto.web.PharmacyResponse;
import com.hospital.entity.Pharmacy;
import com.hospital.repository.PharmacyRepository;
import com.hospital.util.DistanceCalculator;

import lombok.RequiredArgsConstructor;

@Service
public class PharmacyService {

	private final PharmacyRepository pharmacyRepository;
	private final PharmacyConverter pharmacyConverter;
	private final DistanceCalculator distanceCalculator;

	@Autowired
	public PharmacyService(PharmacyRepository pharmacyRepository, PharmacyConverter pharmacyConverter,
			DistanceCalculator distanceCalculator) {
		this.distanceCalculator = distanceCalculator;
		this.pharmacyConverter = pharmacyConverter;
		this.pharmacyRepository = pharmacyRepository;

	}

	@Cacheable(value = "pharmacies", key = "#userLat + '_' + #userLng + '_' + #radius")
	public List<PharmacyResponse> getPharmaciesByDistance(double userLat, double userLng, double radius) {
		List<Pharmacy> allPharmacies = pharmacyRepository.findAll();

		return allPharmacies.stream()
				.filter(pharmacy -> pharmacy.getLatitude() != null && pharmacy.getLongitude() != null).map(pharmacy -> {
					PharmacyResponse response = pharmacyConverter.toResponse(pharmacy);
					double distance = distanceCalculator.calculateDistance(userLat, userLng, response.getCoordinateY(),
							response.getCoordinateX());
					// 거리를 임시로 저장할 수 있는 방법이 필요하거나, 아래처럼 한번에 처리
					return new PharmacyWithDistance(response, distance);
				}).filter(pwd -> pwd.distance <= radius) // 반경 필터링
				.sorted((p1, p2) -> Double.compare(p1.distance, p2.distance)) // 거리순 정렬
				.map(pwd -> pwd.response) // 다시 PharmacyResponse만 추출
				.collect(Collectors.toList());
	}

	// 거리와 함께 임시로 저장하는 내부 클래스
	private static class PharmacyWithDistance {
		final PharmacyResponse response;
		final double distance;

		PharmacyWithDistance(PharmacyResponse response, double distance) {
			this.response = response;
			this.distance = distance;
		}
	}
}

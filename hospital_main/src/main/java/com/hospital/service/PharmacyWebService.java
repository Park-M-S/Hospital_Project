package com.hospital.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.hospital.converter.PharmacyConverter;
import com.hospital.dto.PharmacyWebResponse;
import com.hospital.entity.Pharmacy;
import com.hospital.repository.PharmacyApiRepository;
import com.hospital.util.DistanceCalculator;



@Service
public class PharmacyWebService {

	private final PharmacyApiRepository pharmacyApiRepository;
	private final PharmacyConverter pharmacyConverter;
	private final DistanceCalculator distanceCalculator;

	@Autowired
	public PharmacyWebService(PharmacyApiRepository pharmacyApiRepository, PharmacyConverter pharmacyConverter,
			DistanceCalculator distanceCalculator) {
		this.distanceCalculator = distanceCalculator;
		this.pharmacyConverter = pharmacyConverter;
		this.pharmacyApiRepository = pharmacyApiRepository;

	}

	//@Cacheable(value = "pharmacies", key = "#userLat + '_' + #userLng + '_' + #radius")
	public List<PharmacyWebResponse> getPharmaciesByDistance(double userLat, double userLng, double radius) {
		List<Pharmacy> allPharmacies = pharmacyApiRepository.findAll();

		return allPharmacies.stream()
				.filter(pharmacy -> pharmacy.getLatitude() != null && pharmacy.getLongitude() != null).map(pharmacy -> {
					PharmacyWebResponse response = pharmacyConverter.convertToDTO(pharmacy);
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
		final PharmacyWebResponse response;
		final double distance;

		PharmacyWithDistance(PharmacyWebResponse response, double distance) {
			this.response = response;
			this.distance = distance;
		}
	}
}

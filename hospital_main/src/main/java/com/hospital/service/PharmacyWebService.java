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

	public List<PharmacyWebResponse> getPharmacies(double userLat, double userLng, double radius) {
		double radiusMeters = radius * 1000;

		double latDegree = radiusMeters / 111320.0;
		double lonDegree = radiusMeters / (111320.0 * Math.cos(Math.toRadians(userLat)));

		List<Pharmacy> pharmacyEntities = pharmacyApiRepository.findPharmaciesWithinBoundingBox(userLat, // lat
				userLng, // lon
				radiusMeters, // radius
				userLat - latDegree, // minLat
				userLat + latDegree, // maxLat
				userLng - lonDegree, // minLon
				userLng + lonDegree // maxLon
		);

		return pharmacyEntities.stream().map(pharmacyConverter::convertToDTO).collect(Collectors.toList());
	}
}

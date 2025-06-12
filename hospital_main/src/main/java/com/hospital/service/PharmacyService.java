package com.hospital.service;

import java.util.List;

import com.hospital.dto.web.PharmacyResponse;


public interface PharmacyService {
	
	List<PharmacyResponse> getPharmaciesByDistance(double userLat, double userLng, double radius);

}

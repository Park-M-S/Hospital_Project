package com.hospital.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hospital.dto.web.HospitalDTO;
import com.hospital.util.DistanceCalculator;

@Component
public class HospitalFilter {
	
	private final DistanceCalculator distanceCalculator;
	
	@Autowired
	public HospitalFilter(DistanceCalculator distanceCalculator) {
		this.distanceCalculator = distanceCalculator;
	}
	
	    public boolean filterByDistance(HospitalDTO hospital, double userLat, double userLng, double radius) {
	    	 double hospitalLat = hospital.getCoordinateY();  // Y좌표가 위도
	         double hospitalLng = hospital.getCoordinateX();  // X좌표가 경도

	         double distance = distanceCalculator.calculateDistance(userLat, userLng, hospitalLat, hospitalLng);

	         return distance <= radius;
	    }

}
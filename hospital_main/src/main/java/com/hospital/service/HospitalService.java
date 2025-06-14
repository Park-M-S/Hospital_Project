package com.hospital.service;

import com.hospital.dto.web.HospitalResponse;
import java.util.List;

public interface HospitalService {

	/**
	 * 진료과목으로 병원 검색 (모든 필터링 포함)
	 */
	List<HospitalResponse> getHospitals(List<String> subs, double userLat, double userLng, double radius,
			List<String> tags);
	List<HospitalResponse> searchHospitalsByName(String hospitalName);
}
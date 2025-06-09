package com.hospital.service;

import java.util.List;

import com.hospital.entity.Hospital;

public interface HospitalMainService {
	int fetchParseAndSaveHospitals();

	List<Hospital> getAllHospitals();
	
	List<String> getAllHospitalCodes();

}

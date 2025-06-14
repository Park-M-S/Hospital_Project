package com.hospital.service;

import java.util.List;

import com.hospital.entity.HospitalMain;

public interface HospitalMainApiService {
	int fetchParseAndSaveHospitals();

	List<HospitalMain> getAllHospitals();
	
	List<String> getAllHospitalCodes();

}

package com.hospital.repository;

import java.util.List;


import com.hospital.entity.HospitalEntity;

public interface HospitalRepository {
	 List<HospitalEntity> getAllHospitals(String sub);

}

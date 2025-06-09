package com.hospital.dao;

import java.util.List;
import com.hospital.model.Hospital;

public interface HospitalDAO {
    List<Hospital> getAllHospitals(); // 병원 위치 전부 가져오기
}
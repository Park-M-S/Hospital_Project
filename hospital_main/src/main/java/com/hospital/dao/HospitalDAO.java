package com.hospital.dao;

import java.util.List;
import com.hospital.dto.HospitalDTO;

public interface HospitalDAO {
    List<HospitalDTO> getAllHospitals(String sub); // 병원 위치 전부 가져오기
}
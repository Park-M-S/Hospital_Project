package com.hospital.service;

import java.util.List;

import com.hospital.dto.web.HospitalDTO;

public interface HospitalService {
    List<HospitalDTO> getHospitals(String sub, double userLat, double userLng, double radius, List<String> tags);
}

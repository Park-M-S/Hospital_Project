package com.hospital.service;

import com.hospital.dto.HospitalDTO;
import java.util.List;

public interface HospitalService {
    List<HospitalDTO> getHospitals(String sub, double userLat, double userLng, double radius);
}
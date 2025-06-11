package com.hospital.service;

import com.hospital.dto.web.HospitalResponseDTO;
import java.util.List;

public interface HospitalService {
    
    /**
     * 진료과목으로 병원 검색 (모든 필터링 포함)
     */
    List<HospitalResponseDTO> getHospitals(List<String> subs, double userLat, double userLng, double radius, List<String> tags);
    
  
}
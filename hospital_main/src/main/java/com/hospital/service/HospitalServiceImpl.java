package com.hospital.service;

import com.hospital.dao.HospitalDAO;
import com.hospital.dto.HospitalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalServiceImpl implements HospitalService {

    private final HospitalDAO hospitalDAO;
    private final double EARTH_RADIUS = 6371; // 지구 반지름 (단위: km)
   

    @Autowired
    public HospitalServiceImpl(HospitalDAO hospitalDAO) {
        this.hospitalDAO = hospitalDAO;
    }

    @Override
    public List<HospitalDTO> getHospitals(String sub, double userLat, double userLng, double radius) {
    	
    	
    	List<HospitalDTO> hospitals = hospitalDAO.getAllHospitals(sub);
    	
    	List<HospitalDTO> nearbyHospitals = hospitals.stream()
                .filter(hospital -> calculateDistance(userLat, userLng, hospital.getCoordinateY(), hospital.getCoordinateX()) <= radius)
                .collect(Collectors.toList());

            return nearbyHospitals;
    	
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c; // km 단위
    }
    // 병원 타입에 맞는 병원 목록을 DB에서 가져오는 예시 메서드  
}
package com.hospital.controller;


import com.hospital.dto.web.HospitalResponseDTO;
import com.hospital.dto.web.PharmacyResponse;
import com.hospital.service.HospitalService;
import com.hospital.service.PharmacyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://56.155.87.205"})
public class HospitalController {

    private final HospitalService hospitalService;
	private final PharmacyService pharmacyService;

    @Autowired
    public HospitalController(HospitalService hospitalService,
    		PharmacyService pharmacyService) {
        this.hospitalService = hospitalService;
        this.pharmacyService = pharmacyService;
    }
    
    
    @GetMapping(value ="/hospitalsData", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HospitalResponseDTO> getHospitals(@RequestParam List<String> subs, @RequestParam double userLat, 
    		                              @RequestParam double userLng ,@RequestParam double radius,      		                              
    		                              @RequestParam(required = false) List<String> tags) {
        return hospitalService.getHospitals(subs, userLat, userLng, radius, tags); // 서비스에서 병원 데이터 가져오기
    }
    @GetMapping(value = "/pharmaciesData", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PharmacyResponse> getNearbyPharmacies(
	        @RequestParam double userLat,
	        @RequestParam double userLng, 
	        @RequestParam double radius) {
	    
	    return pharmacyService.getPharmaciesByDistance(userLat, userLng, radius);
	}
    
    // ✅ 병원명 검색 API (태그 필터링 옵션)
    @GetMapping(value = "/hospitals/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HospitalResponseDTO> searchHospitalsByName(
            @RequestParam String hospitalName) {
        return hospitalService.searchHospitalsByName(hospitalName);
    }
}

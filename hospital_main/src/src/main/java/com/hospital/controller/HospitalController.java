package com.hospital.controller;

import com.hospital.dto.HospitalWebResponse;
import com.hospital.dto.PharmacyWebResponse;
import com.hospital.service.HospitalWebService;
import com.hospital.service.PharmacyWebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



//병원,약국 조회
@RestController
@CrossOrigin(origins = "http://localhost:5173") 
public class HospitalController {
	
	private static final Logger log = LoggerFactory.getLogger(HospitalController.class);

    private final HospitalWebService hospitalService;
    private final PharmacyWebService pharmacyService;

    @Autowired
    public HospitalController(HospitalWebService hospitalService, PharmacyWebService pharmacyService) {
        this.hospitalService = hospitalService;
        this.pharmacyService = pharmacyService;
    }
    
    
    //병원 검색 
    @GetMapping(value = "/hospitalsData", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HospitalWebResponse> getHospitals(
        @RequestParam double userLat,
        @RequestParam double userLng, 
        @RequestParam double radius,
        @RequestParam(required = false) List<String> tags
    ) {
        log.info("병원 검색 API 호출 - 위도: {}, 경도: {}, 반경: {}km, 태그: {}", 
                userLat, userLng, radius, tags);
        
        List<HospitalWebResponse> result = hospitalService.getHospitals(userLat, userLng, radius, tags);
        
        log.info("병원 검색 완료 - 조회된 병원 수: {}개", result.size());
        
        return result;
    }

    // 약국 검색 API
    @GetMapping(value = "/pharmaciesData", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PharmacyWebResponse> getNearbyPharmacies(
        @RequestParam double userLat,
        @RequestParam double userLng,
        @RequestParam double radius
    ) {
        log.info("약국 검색 API 호출 - 위도: {}, 경도: {}, 반경: {}km", 
                userLat, userLng, radius);
        
        List<PharmacyWebResponse> result = pharmacyService.getPharmaciesByDistance(userLat, userLng, radius);
        
        log.info("약국 검색 완료 - 조회된 약국 수: {}개", result.size());
        
        return result;
    }

    
    
    //병원명 검색 
    @GetMapping(value = "/hospitals/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HospitalWebResponse> searchHospitalsByName(
            @RequestParam String hospitalName     // 검색할 병원명
    ) {
        return hospitalService.searchHospitalsByName(hospitalName);
    }
    @GetMapping(value = "/hospitals/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HospitalWebResponse> getAllHospitals() {
        return hospitalService.getAllHospitals();
    }
}
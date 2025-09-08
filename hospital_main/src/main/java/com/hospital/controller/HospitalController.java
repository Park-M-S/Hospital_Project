package com.hospital.controller;

import com.hospital.dto.HospitalWebResponse;
import com.hospital.dto.PharmacyWebResponse;
import com.hospital.service.HospitalWebService;
import com.hospital.service.PharmacyWebService;

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
            @RequestParam List<String> subs,      // 진료과목 리스트 (예: ["내과", "외과"])
            @RequestParam double userLat,         // 사용자 위도
            @RequestParam double userLng,         // 사용자 경도
            @RequestParam double radius,          // 검색 반경 (km)
            @RequestParam(required = false) List<String> tags // 추가 필터 (선택사항)
    ) {
        return hospitalService.getHospitals(subs, userLat, userLng, radius, tags);
    }
    
    
    //약국 검색 API
    @GetMapping(value = "/pharmaciesData", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PharmacyWebResponse> getNearbyPharmacies(
            @RequestParam double userLat,         // 사용자 위도
            @RequestParam double userLng,         // 사용자 경도
            @RequestParam double radius           // 검색 반경 (km)
    ) {
        return pharmacyService.getPharmaciesByDistance(userLat, userLng, radius);
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
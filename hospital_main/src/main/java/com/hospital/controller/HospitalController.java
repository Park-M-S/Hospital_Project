package com.hospital.controller;


import com.hospital.dto.api.HospitalWebResponse;
import com.hospital.dto.api.PharmacyWebResponse;
import com.hospital.service.HospitalWebService;
import com.hospital.service.PharmacyWebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class HospitalController {

    private final HospitalWebService hospitalService;
	private final PharmacyWebService pharmacyService;

    @Autowired
    public HospitalController(HospitalWebService hospitalService,
    		PharmacyWebService pharmacyService) {
        this.hospitalService = hospitalService;
        this.pharmacyService = pharmacyService;
    }
    
    
    @GetMapping(value ="/hospitalsData", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HospitalWebResponse> getHospitals(@RequestParam List<String> subs, @RequestParam double userLat, 
    		                              @RequestParam double userLng ,@RequestParam double radius,      		                              
    		                              @RequestParam(required = false) List<String> tags) {
        return hospitalService.getHospitals(subs, userLat, userLng, radius, tags); // 서비스에서 병원 데이터 가져오기
    }
    
    @GetMapping(value = "/pharmaciesData", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PharmacyWebResponse> getNearbyPharmacies(
	        @RequestParam double userLat,
	        @RequestParam double userLng, 
	        @RequestParam double radius) {
	    
	    return pharmacyService.getPharmaciesByDistance(userLat, userLng, radius);
	}
    

    @GetMapping(value = "/hospitals/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HospitalWebResponse> searchHospitalsByName(
            @RequestParam String hospitalName) {
        return hospitalService.searchHospitalsByName(hospitalName);
    }
}
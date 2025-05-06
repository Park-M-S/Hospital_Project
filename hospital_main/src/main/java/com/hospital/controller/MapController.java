package com.hospital.controller;

import com.hospital.dto.HospitalDTO;
import com.hospital.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class MapController {

    private final HospitalService hospitalService;

    @Autowired
    public MapController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping("/map")
    public String showMapPage() {
        return "map"; // /WEB-INF/views/map.jsp로 이동 (ViewResolver 설정 기준)
    }
    
    
    @GetMapping("/mapData")
    public List<HospitalDTO> getHospitals(@RequestParam String sub, @RequestParam double userLat, @RequestParam double userLng ,@RequestParam double radius) {
        return hospitalService.getHospitals(sub, userLat, userLng, radius); // 서비스에서 병원 데이터 가져오기
    }
}
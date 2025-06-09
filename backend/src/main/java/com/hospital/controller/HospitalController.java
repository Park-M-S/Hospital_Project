package com.hospital.controller;

import com.hospital.dto.HospitalDTO;
import com.hospital.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class HospitalController {

    private final HospitalService hospitalService;

    @Autowired
    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping("/hospitals")
    public List<HospitalDTO> getHospitals(
            @RequestParam String sub,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radius,
            @RequestParam List<String> tags // 예: tags=내과&tags=소아과
    ) {
        return hospitalService.getHospitals(sub, lat, lng, radius, tags);
    }
}

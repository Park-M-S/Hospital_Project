package com.hospital.controller;

import com.hospital.service.HospitalApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hospital")
public class HospitalApiController {

    private static final Logger logger = LoggerFactory.getLogger(HospitalApiController.class);

    @Autowired
    private HospitalApiService hospitalApiService;

    @GetMapping("/list")
    public String getHospitalData() {
        logger.info("Received request for /api/hospital/list");
        String result = hospitalApiService.fetchHospitalData();
        logger.info("Response from HospitalApiService: {}", result);
        return result;
    }
}
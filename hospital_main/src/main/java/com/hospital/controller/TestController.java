package com.hospital.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.dto.TestInfoDTO;

@RestController
@CrossOrigin(origins = "http://localhost:5173")  // CORS 설정: Vue.js에서 오는 요청을 허용
public class TestController {

    @GetMapping("/test")  // API 경로
    public List<TestInfoDTO> getMedical_info() {
    	return Arrays.asList(
	        new TestInfoDTO(0, "내과", "fa-solid fa-stethoscope", "Internal Medicine"),
	        new TestInfoDTO(1, "외과", "fa-solid fa-heart", "Surgery"),
	        new TestInfoDTO(2, "신경과", "fa-solid fa-brain", "Neurology"),
	        new TestInfoDTO(3, "정형외과", "fa-solid fa-bone", "Orthopedics"),
	        new TestInfoDTO(4, "안과", "fa-solid fa-eye", "Ophthalmology"),
	        new TestInfoDTO(5, "소화기내과", "fa-solid fa-head-side-cough", "Gastroenterology"),
	        new TestInfoDTO(6, "호흡기내과", "fa-solid fa-circle-radiation", "Respiratory Medicine"),
	        new TestInfoDTO(7, "심장내과", "fa-solid fa-skull", "Cardiology")
		);
    }
    
}
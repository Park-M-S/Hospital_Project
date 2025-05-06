package com.springmvc.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springmvc.domain.medical_info;

@RestController
@CrossOrigin(origins = "http://localhost:5173")  // CORS 설정: Vue.js에서 오는 요청을 허용
public class HomeController {

    @GetMapping("/")  // API 경로
    public List<medical_info> getMedical_info() {
    	return Arrays.asList(
	        new medical_info(0, "내과", "fa-solid fa-stethoscope", "Internal Medicine"),
	        new medical_info(1, "외과", "fa-solid fa-heart", "Surgery"),
	        new medical_info(2, "신경과", "fa-solid fa-brain", "Neurology"),
	        new medical_info(3, "정형외과", "fa-solid fa-bone", "Orthopedics"),
	        new medical_info(4, "안과", "fa-solid fa-eye", "Ophthalmology"),
	        new medical_info(5, "소화기내과", "fa-solid fa-head-side-cough", "Gastroenterology"),
	        new medical_info(6, "호흡기내과", "fa-solid fa-circle-radiation", "Respiratory Medicine"),
	        new medical_info(7, "심장내과", "fa-solid fa-skull", "Cardiology")
		);
    }
    
}

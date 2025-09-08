package com.hospital.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.async.MedicalSubjectAsyncRunner;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.repository.MedicalSubjectApiRepository;

import lombok.extern.slf4j.Slf4j;





//병원별 진료과목 정보 수집 및 저장 기능 구현체

@Service
@Slf4j
public class MedicalSubjectApiService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final MedicalSubjectAsyncRunner medicalSubjectAsyncRunner;
	private final MedicalSubjectApiRepository medicalSubjectApiRepository;

	@Autowired
	public MedicalSubjectApiService(HospitalMainApiRepository hospitalMainApiRepository,
			MedicalSubjectAsyncRunner medicalSubjectAsyncRunner,
			MedicalSubjectApiRepository medicalSubjectApiRepository) {
		this.medicalSubjectApiRepository = medicalSubjectApiRepository;
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.medicalSubjectAsyncRunner = medicalSubjectAsyncRunner;
	}

	public int fetchParseAndSaveMedicalSubjects() {
		try {
			medicalSubjectApiRepository.deleteAllSubjects();
			medicalSubjectApiRepository.resetAutoIncrement();

			List<String> hospitalCodes = hospitalMainApiRepository.findAllHospitalCodes();
			if (hospitalCodes.isEmpty()) {
				throw new IllegalStateException("병원 기본정보가 없어 진료과목을 수집할 수 없습니다");
			}

			medicalSubjectAsyncRunner.setTotalCount(hospitalCodes.size());

			for (String code : hospitalCodes) {
				medicalSubjectAsyncRunner.runAsync(code);
			}

			return hospitalCodes.size();
			
		} catch (Exception e) {
			log.error("진료과목 수집 실패", e);
			throw new RuntimeException("진료과목 수집 중 오류 발생: " + e.getMessage(), e);
		}
	}

	public int getCompletedCount() {
		return medicalSubjectAsyncRunner.getCompletedCount();
	}

	public int getFailedCount() {
		return medicalSubjectAsyncRunner.getFailedCount();
	}
}

package com.hospital.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.async.MedicalSubjectAsyncRunner;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.repository.MedicalSubjectApiRepository;



/**
 * 
 * 병원별 진료과목 정보 수집 및 저장 기능 구현체
 */
@Service

public class MedicalSubjectApiService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final MedicalSubjectAsyncRunner medicalSubjectAsyncRunner; // 진료과목 비동기 실행기
	private final MedicalSubjectApiRepository medicalSubjectApiRepository;

	@Autowired
	public  MedicalSubjectApiService(HospitalMainApiRepository hospitalMainApiRepository,
			MedicalSubjectAsyncRunner medicalSubjectAsyncRunner,
			MedicalSubjectApiRepository medicalSubjectApiRepository) {
		
		this.medicalSubjectApiRepository = medicalSubjectApiRepository;
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.medicalSubjectAsyncRunner = medicalSubjectAsyncRunner;

	}

	public int fetchParseAndSaveMedicalSubjects() {
		medicalSubjectApiRepository.deleteAllSubjects();

		medicalSubjectApiRepository.resetAutoIncrement();

		List<String> hospitalCodes = hospitalMainApiRepository.findAllHospitalCodes();

		medicalSubjectAsyncRunner.setTotalCount(hospitalCodes.size()); // 전체 작업 수 등록

		for (String code : hospitalCodes) {
			medicalSubjectAsyncRunner.runAsync(code); // 병렬 실행 (스레드 풀 사용)
		}

		return hospitalCodes.size(); // 실행한 병원 수 반환
	}

	/**
	 * 저장 완료 수 조회
	 */

	public int getCompletedCount() {
		return medicalSubjectAsyncRunner.getCompletedCount();
	}

	/**
	 * 실패한 병원 수 조회
	 */

	public int getFailedCount() {
		return medicalSubjectAsyncRunner.getFailedCount();
	}
}
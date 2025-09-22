package com.hospital.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.async.MedicalSubjectAsyncRunner;
import com.hospital.config.SubjectMappingConfig;
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
	private final SubjectMappingConfig subjectMappingConfig;

	@Autowired
	public MedicalSubjectApiService(HospitalMainApiRepository hospitalMainApiRepository,
			MedicalSubjectAsyncRunner medicalSubjectAsyncRunner,
			MedicalSubjectApiRepository medicalSubjectApiRepository,
			SubjectMappingConfig subjectMappingConfig) {
		this.medicalSubjectApiRepository = medicalSubjectApiRepository;
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.medicalSubjectAsyncRunner = medicalSubjectAsyncRunner;
		this.subjectMappingConfig = subjectMappingConfig;
	}

	public int updateSubjects() {
		try {
			log.info("병원 데이터 수집 시작 - 진료과목: {}", subjectMappingConfig.getSubjectNames());
			
			log.info("기존 과목 데이터 전체 삭제 시작...");
	        medicalSubjectApiRepository.deleteAll();
	        log.info("기존 과목 데이터 전체 삭제 완료");
			
			List<String> subCodes = subjectMappingConfig.getSubjectCodes();
			medicalSubjectAsyncRunner.resetCounter();
			medicalSubjectAsyncRunner.setTotalCount(subCodes.size());

			for (String subCd : subCodes) {
				medicalSubjectAsyncRunner.runAsync(subCd);
			}
			log.info("{}개 과목 병렬 처리 시작", subCodes.size());
			return subCodes.size(); // 총 지역 수만 반환

			
			
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

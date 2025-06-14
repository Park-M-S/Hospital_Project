package com.hospital.service;

import com.hospital.async.MedicalSubjectApiAsyncRunner;
import com.hospital.repository.MedicalSubjectApiRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 🧠 MedicalSubjectServiceImpl
 * 병원별 진료과목 정보 수집 및 저장 기능 구현체
 */
@Service
public class MedicalSubjectApiServiceImpl implements MedicalSubjectApiService {

    private final HospitalMainApiService hospitalMainService;      // 병원 코드 조회용 서비스
    private final MedicalSubjectApiAsyncRunner asyncRunner;        // 진료과목 비동기 실행기
    private final MedicalSubjectApiRepository medicalSubjectRepository;

    @Autowired
    public MedicalSubjectApiServiceImpl(HospitalMainApiService hospitalMainService,
                                     MedicalSubjectApiAsyncRunner asyncRunner,
                                     MedicalSubjectApiRepository medicalSubjectRepository) {
        this.hospitalMainService = hospitalMainService;
        this.asyncRunner = asyncRunner;
        this.medicalSubjectRepository = medicalSubjectRepository;
    }

    /**
     * ✅ 병원 전체 대상 진료과목 정보 수집 시작
     * 1. 병원 코드 리스트 조회
     * 2. AsyncRunner에 전체 수 설정
     * 3. 병원코드별로 비동기 실행
     *
     * @return 전체 병원 수 (작업 수)
     */
    @Override
    public int fetchParseAndSaveMedicalSubjects() {
    	 medicalSubjectRepository.deleteAllSubjects();

         medicalSubjectRepository.resetAutoIncrement();
    	
        List<String> hospitalCodes = hospitalMainService.getAllHospitalCodes();

        asyncRunner.setTotalCount(hospitalCodes.size()); // 전체 작업 수 등록

        for (String code : hospitalCodes) {
            asyncRunner.runAsync(code); // ✅ 병렬 실행 (스레드 풀 사용)
        }

        return hospitalCodes.size(); // 실행한 병원 수 반환
    }

    /**
     * ✅ 저장 완료 수 조회
     */
    @Override
    public int getCompletedCount() {
        return asyncRunner.getCompletedCount();
    }

    /**
     * ✅ 실패한 병원 수 조회
     */
    @Override
    public int getFailedCount() {
        return asyncRunner.getFailedCount();
    }
}
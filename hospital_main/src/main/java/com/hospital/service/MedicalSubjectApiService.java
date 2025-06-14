package com.hospital.service;

public interface MedicalSubjectApiService {

    /**
     * 병원 코드 리스트를 기반으로 공공 API에서 진료과목 정보를 받아와 DB에 저장합니다.
     * 비동기 방식으로 각 병원마다 병렬로 처리됩니다.
     * 
     * @return 전체 병원 수
     */
    int fetchParseAndSaveMedicalSubjects();

    /**
     * 비동기 처리된 병원 수 (성공 카운트)
     * @return 성공 처리 수
     */
    int getCompletedCount();

    /**
     * 비동기 처리 중 실패한 병원 수
     * @return 실패 처리 수
     */
    int getFailedCount();
}

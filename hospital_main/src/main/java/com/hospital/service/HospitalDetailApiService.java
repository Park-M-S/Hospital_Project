package com.hospital.service;

public interface HospitalDetailApiService {
    
    /**
     * 병원 전체를 대상으로 API 호출 후 병원 상세 데이터 비동기 저장 실행
     * @return 처리 대상 병원 수
     */
    int updateAllHospitalDetails();
    
    /**
     * 완료된 병원 처리 수 조회
     * @return 완료된 작업 수
     */
    int getCompletedCount();
    
    /**
     * 실패한 병원 처리 수 조회
     * @return 실패한 작업 수
     */
    int getFailedCount();
    
    /**
     * 전체 작업 수 조회
     * @return 전체 작업 수
     */
    int getTotalCount();
}
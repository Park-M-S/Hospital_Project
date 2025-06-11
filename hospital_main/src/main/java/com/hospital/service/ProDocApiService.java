package com.hospital.service;

/**
 * 🏥 ProDocService
 * 전문의(ProDoc) 정보 관련 비즈니스 로직 인터페이스
 */
public interface ProDocApiService {

    /**
     * ✅ 전체 병원 대상 전문의 데이터 API 호출 → 파싱 → 저장
     * 비동기 방식으로 실행되며, 병원별 데이터를 병렬 처리함.
     * @return 전체 병원 수 (작업 대상 수)
     */
    int fetchParseAndSaveProDocs();

    /**
     * ✅ 완료된 병원 처리 건수 반환
     * @return 완료된 병원 수
     */
    int getCompletedCount();

    /**
     * ✅ 실패한 병원 처리 건수 반환
     * @return 실패한 병원 수
     */
    int getFailedCount();
}

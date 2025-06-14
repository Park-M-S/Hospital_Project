package com.hospital.repository;

import com.hospital.entity.MedicalSubject;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;



import java.util.List;





public interface MedicalSubjectApiRepository extends JpaRepository<MedicalSubject, Long> {



    // ✅ 병원코드로 해당 병원의 모든 진료과목 조회

    List<MedicalSubject> findByHospitalCode(String hospitalCode);



    // ✅ 병원코드 리스트로 해당 병원들의 모든 진료과목 조회

    List<MedicalSubject> findByHospitalCodeIn(List<String> hospitalCodes);



    // ✅ 병원코드 + 과목명으로 중복 확인

    boolean existsByHospitalCodeAndSubjectName(String hospitalCode, String subjectName);



    // ✅ 병원코드 기준 기존 진료과목 전체 삭제

    void deleteByHospitalCode(String hospitalCode);



    // ✅ 전체 삭제 (FK 안전)

    @Modifying

    @Transactional

    @Query(value = "DELETE FROM medical_subject", nativeQuery = true)

    void deleteAllSubjects();



    // ✅ AUTO_INCREMENT 초기화

    @Modifying

    @Transactional

    @Query(value = "ALTER TABLE medical_subject AUTO_INCREMENT = 1", nativeQuery = true)

    void resetAutoIncrement();

}

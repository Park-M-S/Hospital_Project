package com.hospital.repository;

import com.hospital.entity.MedicalSubject;

import jakarta.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MedicalSubjectApiRepository extends JpaRepository<MedicalSubject, Long> {

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	List<MedicalSubject> findByHospitalCode(String hospitalCode);

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	List<MedicalSubject> findByHospitalCodeIn(List<String> hospitalCodes);


	// 병원코드 기준 기존 진료과목 전체 삭제
	void deleteByHospitalCode(String hospitalCode);
	
	@Modifying
    @Transactional
    void deleteByHospitalCodeIn(List<String> hospitalCodes);

	//전체 삭제 (FK 안전)
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM medical_subject", nativeQuery = true)

	void deleteAllSubjects();

	// AUTO_INCREMENT 초기화
	@Modifying
	@Transactional
	@Query(value = "ALTER TABLE medical_subject AUTO_INCREMENT = 1", nativeQuery = true)
	void resetAutoIncrement();
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@Query("SELECT DISTINCT s.hospitalCode FROM MedicalSubject s")
	List<String> findAllDistinctHospitalCodes();

}

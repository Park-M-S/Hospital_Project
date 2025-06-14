package com.hospital.repository;

import com.hospital.entity.HospitalMain;
import com.hospital.entity.MedicalSubject;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;



public interface HospitalRepository extends JpaRepository<HospitalMain, String>, 
                                          JpaSpecificationExecutor<HospitalMain> {

    // 방법 1: Specification을 사용한 동적 쿼리 (추천)
	@EntityGraph(attributePaths = {"hospitalDetail", "proDocs"})
	@Query("SELECT h FROM HospitalMain h WHERE " +
	       "(SELECT COUNT(DISTINCT ms.subjectName) FROM h.medicalSubjects ms " +
	       " WHERE ms.subjectName IN :subjects) = :#{#subjects.size()}")
	List<HospitalMain> findHospitalsBySubjects(@Param("subjects") List<String> subs);
	
	  // ✅ 병원명 검색 (hospitalDetail만 EAGER FETCH)
    @EntityGraph(attributePaths = {"hospitalDetail"})
    @Query("SELECT h FROM HospitalMain h WHERE REPLACE(h.hospitalName, ' ', '') LIKE %:hospitalName%")
    List<HospitalMain> findHospitalsByName(@Param("hospitalName") String hospitalName);
}
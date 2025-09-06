package com.hospital.repository;

import com.hospital.entity.HospitalMain;

import jakarta.persistence.QueryHint;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface HospitalMainApiRepository extends JpaRepository<HospitalMain, String> {

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@Query("SELECT h.hospitalCode FROM HospitalMain h")
	List<String> findAllHospitalCodes();
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@EntityGraph("hospital-with-all")
	Optional<HospitalMain> findByHospitalCode(String hospitalCode);
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@Query("SELECT h FROM HospitalMain h WHERE REPLACE(h.hospitalName, ' ', '') LIKE CONCAT('%', REPLACE(:hospitalName, ' ', ''), '%')")
	List<HospitalMain> findByHospitalNameContaining(@Param("hospitalName") String hospitalName);
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@EntityGraph("hospital-with-all")
	@Query("SELECT DISTINCT h FROM HospitalMain h WHERE "
			+ "(SELECT COUNT(DISTINCT ms.subjectName) FROM h.medicalSubjects ms "
			+ " WHERE ms.subjectName IN :subjects) = :#{#subjects.size()}")
	List<HospitalMain> findHospitalsBySubjects(@Param("subjects") List<String> subjects);
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@EntityGraph("hospital-with-detail")
	@Query("SELECT DISTINCT h FROM HospitalMain h WHERE REPLACE(h.hospitalName, ' ', '') LIKE %:hospitalName%")
	List<HospitalMain> findHospitalsByName(@Param("hospitalName") String hospitalName);
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@EntityGraph("hospital-with-all")
	@Query("SELECT DISTINCT h FROM HospitalMain h")
	@Override
	List<HospitalMain> findAll();
	
	//@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	
}

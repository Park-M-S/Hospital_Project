package com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.entity.HospitalDetail;
import com.hospital.entity.HospitalMain;

import jakarta.persistence.QueryHint;


public interface HospitalDetailApiRepository extends JpaRepository<HospitalDetail, String> {
  
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM hospital_detail", nativeQuery = true)
	void deleteAllDetails();
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	List<HospitalDetail> findByHospitalCodeIn(@Param("hospitalCodes") List<String> hospitalCodes);
	
}
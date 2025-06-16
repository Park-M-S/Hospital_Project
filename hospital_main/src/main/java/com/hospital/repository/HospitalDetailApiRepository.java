package com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.entity.HospitalDetail;


public interface HospitalDetailApiRepository extends JpaRepository<HospitalDetail, String> {
  
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM hospital_detail", nativeQuery = true)
	void deleteAllDetails();
	
}
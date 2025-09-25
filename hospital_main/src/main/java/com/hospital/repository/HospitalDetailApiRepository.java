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
	List<HospitalDetail> findByHospitalCodeIn(List<String> hospitalCodes);

	@Modifying
	@Transactional
	void deleteByHospitalCodeIn(List<String> hospitalCodes);
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Query("SELECT h.hospitalCode FROM HospitalDetail h")
    List<String> findAllHospitalCodes();
	
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@Query("SELECT DISTINCT h.hospitalCode FROM HospitalDetail h")
	List<String> findAllDistinctHospitalCodes();


}
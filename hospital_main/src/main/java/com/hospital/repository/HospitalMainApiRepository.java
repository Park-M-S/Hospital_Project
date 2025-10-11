package com.hospital.repository;

import com.hospital.dto.HospitalWebResponse;
import com.hospital.entity.HospitalMain;

import jakarta.persistence.QueryHint;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
	@EntityGraph("hospital-with-all")
	@Query("SELECT DISTINCT h FROM HospitalMain h " + "WHERE LENGTH(REPLACE(:searchName, ' ', '')) >= 3 "
			+ "AND REPLACE(h.hospitalName, ' ', '')" + "LIKE CONCAT('%', replace(:searchName, ' ', ''), '%')")
	List<HospitalMain> findHospitalsByName(@Param("searchName") String searchName);

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@EntityGraph("hospital-with-all")
	@Query("SELECT h FROM HospitalMain h")
	@Override
	List<HospitalMain> findAll();

	@Modifying
	@Transactional
	List<HospitalMain> deleteByHospitalCodeIn(List<String> hospitalcodes);


	//@EntityGraph("hospital-with-all")
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	List<HospitalMain> findByHospitalCodeIn(List<String> hospitalCodes);

	

}
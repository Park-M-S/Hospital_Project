package com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.entity.Pharmacy;

import jakarta.persistence.QueryHint;

public interface PharmacyApiRepository extends JpaRepository<Pharmacy, Long> {
	boolean existsByYkiho(String ykiho);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM pharmacy", nativeQuery = true)
	void deleteAllPharmacies();

	// ✅ AUTO_INCREMENT 초기화

	@Modifying

	@Transactional

	@Query(value = "ALTER TABLE pharmacy AUTO_INCREMENT = 1", nativeQuery = true)

	void resetAutoIncrement();

	@Transactional
	@Modifying
	void deleteByYkihoIn(List<String> ykihoList);

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@Query("SELECT DISTINCT p FROM Pharmacy p " +
	       "WHERE LENGTH(REPLACE(:hospitalName, ' ', '')) >= 3 " +
	       "AND REPLACE(p.name, ' ', '') " +
	       "LIKE CONCAT('%', REPLACE(:hospitalName, ' ', ''), '%')")
	List<Pharmacy> findPharmacyByName(@Param("hospitalName") String hospitalName);
}

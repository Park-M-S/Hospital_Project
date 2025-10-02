package com.hospital.repository;

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


	/*
	 * @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	 * 
	 * @EntityGraph("hospital-with-all")
	 * 
	 * @Query("SELECT DISTINCT h FROM HospitalMain h WHERE " +
	 * "(SELECT COUNT(DISTINCT ms.subjectName) FROM h.medicalSubjects ms " +
	 * " WHERE ms.subjectName IN :subjects) = :#{#subjects.size()}")
	 * List<HospitalMain> findHospitalsBySubjects(@Param("subjects") List<String>
	 * subjects);
	 */

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@EntityGraph("hospital-with-all")
	@Query("SELECT DISTINCT h FROM HospitalMain h " +
	       "WHERE LENGTH(REPLACE(:searchName, ' ', '')) >= 3 " +
		   "AND REPLACE(h.hospitalName, ' ', '')" +
	       "LIKE CONCAT('%', replace(:searchName, ' ', ''), '%')")
	List<HospitalMain> findHospitalsByName(@Param("searchName") String searchName);
	

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@EntityGraph("hospital-with-all")
	@Query("SELECT h FROM HospitalMain h")
	@Override
	List<HospitalMain> findAll();

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	List<HospitalMain> findByDistrictName(String districtName);

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	List<HospitalMain> findByHospitalCodeIn(List<String> hospitalCodes);

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	List<HospitalMain> findByProvinceName(String provinceName);

	@Modifying
	@Transactional
	List<HospitalMain> deleteByHospitalCodeIn(List<String> hospitalcodes);

	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@EntityGraph("hospital-with-all")
	@Query("SELECT h FROM HospitalMain h " +
	       "WHERE h.coordinateX BETWEEN :minLon AND :maxLon " +
	       "AND h.coordinateY BETWEEN :minLat AND :maxLat")
	List<HospitalMain> findHospitalsWithinBoundingBox(
	        @Param("minLat") double minLat,
	        @Param("maxLat") double maxLat,
	        @Param("minLon") double minLon,
	        @Param("maxLon") double maxLon
	);

	// 성능 비교용 - ST_Distance_Sphere만 사용 (인덱스 미사용)
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@Query(value = """
	        SELECT h.*
	        FROM hospital_main h
	        WHERE ST_Distance_Sphere(
	                  point(h.coordinate_x, h.coordinate_y),
	                  point(:lon, :lat)
	              ) <= :radius * 1000
	        AND h.coordinate_x IS NOT NULL
	        AND h.coordinate_y IS NOT NULL
	        """, nativeQuery = true)
	List<HospitalMain> findHospitalsWithinRadius(
	        @Param("lat") double lat,
	        @Param("lon") double lon,
	        @Param("radius") double radius);
}
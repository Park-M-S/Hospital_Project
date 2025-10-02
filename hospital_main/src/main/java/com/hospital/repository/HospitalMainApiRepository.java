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
	       "WHERE LENGTH(REPLACE(:hospitalName, ' ', '')) >= 3 " +
		   "AND REPLACE(h.hospitalName, ' ', '')" +
	       "LIKE CONCAT('%', replace(:hospitalName, ' ', ''), '%')")
	List<HospitalMain> findHospitalsByName(@Param("hospitalName") String hospitalName);
	

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

	 @Query(value = """
		        SELECT h.*
		        FROM hospital_main h
		        WHERE h.coordinate_x BETWEEN :minLon AND :maxLon
		          AND h.coordinate_y BETWEEN :minLat AND :maxLat
		          AND ST_Distance_Sphere(POINT(h.coordinate_x, h.coordinate_y), POINT(:lon, :lat)) <= :radius
		        """, nativeQuery = true)
		    List<HospitalMain> findHospitalsWithinBoundingBox(
		    	    @Param("lat") double lat,           // 추가 필요
		    	    @Param("lon") double lon,           // 추가 필요
		    	    @Param("radius") double radius,
		            @Param("minLat") double minLat,
		            @Param("maxLat") double maxLat,
		            @Param("minLon") double minLon,
		            @Param("maxLon") double maxLon
		    );
}
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

	@Query(value = """
		    SELECT h.*
		    FROM hospital_main h
		    WHERE MBRContains(
		        ST_GeomFromText(
		            CONCAT('POLYGON((', 
		                :min_lon, ' ', :min_lat, ',',
		                :max_lon, ' ', :min_lat, ',',
		                :max_lon, ' ', :max_lat, ',',
		                :min_lon, ' ', :max_lat, ',',
		                :min_lon, ' ', :min_lat, '))'
		            ),
		            4326
		        ),
		        h.location
		    )
		    """, nativeQuery = true)
		@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
		List<HospitalMain> findByMBRDirect(
		    @Param("min_lon") double minLon,
		    @Param("max_lon") double maxLon,
		    @Param("min_lat") double minLat,
		    @Param("max_lat") double maxLat
		);

	//@EntityGraph("hospital-with-all")
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	List<HospitalMain> findByHospitalCodeIn(List<String> hospitalCodes);

	// 성능 비교용 - ST_Distance_Sphere만 사용 (인덱스 미사용)
	// Step1: 거리 조건만으로 병원코드(PK) 리스트 조회
	@QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
	@Query(value = """
	    SELECT h.*
	    FROM hospital_main h
	    WHERE h.coordinate_x BETWEEN :minLon AND :maxLon
	      AND h.coordinate_y BETWEEN :minLat AND :maxLat
	      AND h.coordinate_x IS NOT NULL
	      AND h.coordinate_y IS NOT NULL
	    """, nativeQuery = true)
	List<HospitalMain> findByMBRDirectWithoutIndex(
	        @Param("minLon") double minLon,
	        @Param("maxLon") double maxLon,
	        @Param("minLat") double minLat,
	        @Param("maxLat") double maxLat
	);
	

}
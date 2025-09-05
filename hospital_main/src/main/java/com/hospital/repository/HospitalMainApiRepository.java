package com.hospital.repository;

import com.hospital.entity.HospitalMain;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface HospitalMainApiRepository extends JpaRepository<HospitalMain, String> {

    @Query("SELECT h.hospitalCode FROM HospitalMain h")
    List<String> findAllHospitalCodes();

 
    @EntityGraph("hospital-with-all")
    Optional<HospitalMain> findByHospitalCode(String hospitalCode);
    
   
 
    @Query("SELECT h FROM HospitalMain h WHERE REPLACE(h.hospitalName, ' ', '') LIKE CONCAT('%', REPLACE(:hospitalName, ' ', ''), '%')")
    List<HospitalMain> findByHospitalNameContaining(@Param("hospitalName") String hospitalName);
    
    @EntityGraph("hospital-with-all")
    @Query("SELECT h FROM HospitalMain h WHERE " +
           "(SELECT COUNT(DISTINCT ms.subjectName) FROM h.medicalSubjects ms " +
           " WHERE ms.subjectName IN :subjects) = :#{#subjects.size()}")
    List<HospitalMain> findHospitalsBySubjects(@Param("subjects") List<String> subjects);
    
  
	@EntityGraph("hospital-with-detail")
    @Query("SELECT h FROM HospitalMain h WHERE REPLACE(h.hospitalName, ' ', '') LIKE %:hospitalName%")
    List<HospitalMain> findHospitalsByName(@Param("hospitalName") String hospitalName);
	
	 @EntityGraph("hospital-with-all")
	    @Override
	    List<HospitalMain> findAll();
}

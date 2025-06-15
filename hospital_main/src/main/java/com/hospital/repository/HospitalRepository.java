package com.hospital.repository;

import com.hospital.entity.HospitalMain;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;




public interface HospitalRepository extends JpaRepository<HospitalMain, String>, 
                                          JpaSpecificationExecutor<HospitalMain> {

	@EntityGraph("hospital-with-detail")
    @Query("SELECT h FROM HospitalMain h WHERE " +
           "(SELECT COUNT(DISTINCT ms.subjectName) FROM h.medicalSubjects ms " +
           " WHERE ms.subjectName IN :subjects) = :#{#subjects.size()}")
    List<HospitalMain> findHospitalsBySubjects(@Param("subjects") List<String> subjects);
    
  
	@EntityGraph("hospital-with-detail")
    @Query("SELECT h FROM HospitalMain h WHERE REPLACE(h.hospitalName, ' ', '') LIKE %:hospitalName%")
    List<HospitalMain> findHospitalsByName(@Param("hospitalName") String hospitalName);
}
package com.hospital.repository;

import com.hospital.entity.HospitalMain;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface HospitalMainApiRepository extends JpaRepository<HospitalMain, String> {

    @Query("SELECT h.hospitalCode FROM HospitalMain h")
    List<String> findAllHospitalCodes();

    // 병원 코드로 병원 조회
    Optional<HospitalMain> findByHospitalCode(String hospitalCode);
    
    @Query("SELECT h FROM HospitalMain h WHERE REPLACE(h.hospitalName, ' ', '') LIKE CONCAT('%', REPLACE(:hospitalName, ' ', ''), '%')")
    List<HospitalMain> findByHospitalNameContaining(@Param("hospitalName") String hospitalName);
}

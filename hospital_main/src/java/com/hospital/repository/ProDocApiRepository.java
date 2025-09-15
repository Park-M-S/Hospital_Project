package com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.entity.ProDoc;

import jakarta.persistence.QueryHint;

public interface ProDocApiRepository extends JpaRepository<ProDoc, Long> {
	


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM pro_doc", nativeQuery = true)
    void deleteAllProDocs();
    
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE pro_doc AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
    
 //조회용 메서드들 (ReadOnly 최적화)
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    List<ProDoc> findByHospitalCode(String hospitalCode);

    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    List<ProDoc> findByHospitalCodeIn(List<String> hospitalCodes);

    //삭제용 메서드들
    @Modifying
    @Transactional
    void deleteByHospitalCode(String hospitalCode);

    @Modifying
    @Transactional
    void deleteByHospitalCodeIn(List<String> hospitalCodes);
    
    @QueryHints({ @QueryHint(name = "org.hibernate.readOnly", value = "true") })
    @Query("SELECT DISTINCT p.hospitalCode FROM ProDoc p")
    List<String> findAllDistinctHospitalCodes();

}

package com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.entity.Pharmacy;


public interface PharmacyApiRepository extends JpaRepository<Pharmacy, Long> {
    boolean existsByYkiho(String ykiho);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM pharmacy", nativeQuery = true)
    void deleteAllPharmacies();
}

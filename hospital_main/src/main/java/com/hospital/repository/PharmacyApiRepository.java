package com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.entity.Pharmacy;


public interface PharmacyApiRepository extends JpaRepository<Pharmacy, Long> {
    boolean existsByYkiho(String ykiho);
}

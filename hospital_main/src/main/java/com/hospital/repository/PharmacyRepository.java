package com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.entity.Pharmacy;
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long>{
	

}

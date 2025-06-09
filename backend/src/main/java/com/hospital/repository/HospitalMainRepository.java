package com.hospital.repository;

import com.hospital.entity.Hospital;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalMainRepository extends JpaRepository<Hospital, String>{


}
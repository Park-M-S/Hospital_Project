package com.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hospital.entity.YouTubeCategory;

public interface YouTubeCategoryRepository extends JpaRepository<YouTubeCategory, String>{
	
	List<YouTubeCategory> findAll();
	
}
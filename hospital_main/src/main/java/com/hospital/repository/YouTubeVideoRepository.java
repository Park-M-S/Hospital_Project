package com.hospital.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.entity.YouTubeVideo;

@Repository
public interface YouTubeVideoRepository extends JpaRepository<YouTubeVideo, String> {
	
    
    /**
     * 카테고리별 조회
     */
    List<YouTubeVideo> findByCategory(String category);
    
    /**
     * 카테고리별 최신순 조회
     */
    List<YouTubeVideo> findByCategoryOrderByPublishedAtDesc(String category);
    
    /**
     * 검색어로 조회
     */
    List<YouTubeVideo> findBySearchQuery(String searchQuery);
    
    /**
     * 카테고리별 개수 조회
     */
    long countByCategory(String category);
    
    /**
     * 제목 또는 설명에 키워드 포함된 영상 검색
     */
    List<YouTubeVideo> findByTitleContainingOrDescriptionContaining(String titleKeyword, String descriptionKeyword);
    
    /**
     * 카테고리 + 제목 검색
     */
    List<YouTubeVideo> findByCategoryAndTitleContaining(String category, String titleKeyword);
    
    /**
     * 카테고리별 페이지네이션 조회
     */
    Page<YouTubeVideo> findByCategory(String category, Pageable pageable);
    
}
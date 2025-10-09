package com.hospital.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.caller.YouTubeApiCaller;
import com.hospital.entity.YouTubeVideo;
import com.hospital.parser.YouTubeVideoParser;
import com.hospital.repository.YouTubeVideoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeVideoService {
    
    private final YouTubeApiCaller youTubeApiCaller;
    private final YouTubeVideoParser youTubeVideoParser;
    private final YouTubeVideoRepository youTubeVideoRepository;
    
    // ==================== API 호출 + 저장 ====================
    
    /**
     * YouTube API 호출 -> 파싱 -> DB 저장
     */
    @Transactional
    public int fetchAndSaveVideos(String query, int maxResults, String category) {
        try {
            log.info("[{}] 영상 검색 시작 - 검색어: {}, maxResults: {}", category, query, maxResults);
            
            // 1. API 호출
            String jsonResponse = youTubeApiCaller.searchMedicalVideos(query, maxResults);
            
            // 2. 파싱
            List<YouTubeVideo> videos = youTubeVideoParser.parseToEntities(jsonResponse, category, query);
            
            if (videos.isEmpty()) {
                log.warn("[{}] 파싱된 영상 없음 - 검색어: {}", category, query);
                return 0;
            }
            
            // 3. 중복 제거 후 저장
            int savedCount = 0;
            for (YouTubeVideo video : videos) {
                if (!youTubeVideoRepository.existsById(video.getVideoId())) {
                    youTubeVideoRepository.save(video);
                    savedCount++;
                } else {
                    log.debug("중복 영상 스킵: {}", video.getVideoId());
                }
            }
            
            log.info("[{}] 영상 저장 완료 - 검색어: {}, 저장: {}/{}", 
                    category, query, savedCount, videos.size());
            
            return savedCount;
            
        } catch (Exception e) {
            log.error("[{}] 영상 저장 실패 - 검색어: {}, 오류: {}", category, query, e.getMessage(), e);
            return 0;
        }
    }
    
    @Transactional(readOnly = true)
    public Page<YouTubeVideo> getVideosByPage(String category, Pageable pageable) {
        if (category != null && !category.isEmpty()) {
            return youTubeVideoRepository.findByCategory(category, pageable);
        }
        return youTubeVideoRepository.findAll(pageable);
    }
    
    /**
     * 모든 영상 삭제
     */
    @Transactional
    public void deleteAllVideos() {
        youTubeVideoRepository.deleteAll();
        log.info("모든 영상 삭제 완료");
    }
    
    // ==================== 조회 ====================
    
    /**
     * 카테고리별 영상 조회
     */
    @Transactional(readOnly = true)
    public List<YouTubeVideo> getVideosByCategory(String category) {
        return youTubeVideoRepository.findByCategory(category);
    }
    
    /**
     * 카테고리별 최신순 영상 조회 (제한)
     */
    @Transactional(readOnly = true)
    public List<YouTubeVideo> getVideosByCategoryOrderByLatest(String category, int limit) {
        return youTubeVideoRepository.findByCategoryOrderByPublishedAtDesc(category)
                .stream()
                .limit(limit)
                .toList();
    }
    
    /**
     * videoId로 영상 조회
     */
    @Transactional(readOnly = true)
    public YouTubeVideo getVideoById(String videoId) {
        return youTubeVideoRepository.findById(videoId).orElse(null);
    }
    
    /**
     * 전체 영상 개수 조회
     */
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return youTubeVideoRepository.count();
    }
    
    /**
     * 카테고리별 영상 개수 조회
     */
    @Transactional(readOnly = true)
    public long getCountByCategory(String category) {
        return youTubeVideoRepository.countByCategory(category);
    }
}
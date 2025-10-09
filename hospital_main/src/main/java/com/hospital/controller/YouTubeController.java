package com.hospital.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.entity.YouTubeVideo;
import com.hospital.service.YouTubeVideoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/youtube")
@RequiredArgsConstructor
public class YouTubeController {
    
    private final YouTubeVideoService youTubeVideoService;
    
    /**
     * YouTube API 호출 -> 파싱 -> DB 저장
     * GET /youtube/fetch?query=내과&maxResults=10&category=DEPT_내과
     */
    @GetMapping(value = "/fetch", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> fetchAndSaveVideos(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "maxResults", defaultValue = "10") int maxResults,
            @RequestParam(value = "category", defaultValue = "GENERAL") String category) {
        
        int savedCount = youTubeVideoService.fetchAndSaveVideos(query, maxResults, category);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("query", query);
        response.put("maxResults", maxResults);
        response.put("category", category);
        response.put("savedCount", savedCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 카테고리별 영상 조회
     * GET /youtube/videos?category=GENERAL
     */
    @GetMapping(value = "/videos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<YouTubeVideo>> getVideosByCategory(
            @RequestParam(value = "category") String category) {
        
        List<YouTubeVideo> videos = youTubeVideoService.getVideosByCategory(category);
        return ResponseEntity.ok(videos);
    }
    
    /**
     * 카테고리별 최신순 영상 조회 (제한)
     * GET /youtube/videos/latest?category=GENERAL&limit=10
     */
    @GetMapping(value = "/videos/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<YouTubeVideo>> getLatestVideos(
            @RequestParam(value = "category") String category,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        List<YouTubeVideo> videos = youTubeVideoService.getVideosByCategoryOrderByLatest(category, limit);
        return ResponseEntity.ok(videos);
    }
    
    /**
     * 영상 상세 조회
     * GET /youtube/videos/{videoId}
     */
    @GetMapping(value = "/videos/{videoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<YouTubeVideo> getVideoById(@PathVariable String videoId) {
        
        YouTubeVideo video = youTubeVideoService.getVideoById(videoId);
        
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(video);
    }
    /**
     * 페이지네이션 영상 조회 (100개씩)
     * GET /youtube/videos/page?page=0&size=100&category=GENERAL
     */
    @GetMapping(value = "/videos/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getVideosByPage(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "100") int size,
            @RequestParam(value = "category", required = false) String category) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<YouTubeVideo> videoPage = youTubeVideoService.getVideosByPage(category, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", videoPage.getContent());           // 실제 데이터
        response.put("currentPage", videoPage.getNumber());        // 현재 페이지 (0부터 시작)
        response.put("totalPages", videoPage.getTotalPages());     // 전체 페이지 수
        response.put("totalElements", videoPage.getTotalElements()); // 전체 데이터 수
        response.put("size", videoPage.getSize());                 // 페이지 크기
        response.put("hasNext", videoPage.hasNext());              // 다음 페이지 있는지
        response.put("hasPrevious", videoPage.hasPrevious());      // 이전 페이지 있는지
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 통계 조회
     * GET /youtube/stats
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(value = "category", required = false) String category) {
        
        Map<String, Object> stats = new HashMap<>();
        
        if (category != null) {
            stats.put("category", category);
            stats.put("count", youTubeVideoService.getCountByCategory(category));
        } else {
            stats.put("totalCount", youTubeVideoService.getTotalCount());
        }
        
        return ResponseEntity.ok(stats);
    }
}
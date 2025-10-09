package com.hospital.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.initializer.YouTubeDataInitializer;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/admin/youtube")
@RequiredArgsConstructor
public class AdminYouTubeController {
    
    private final YouTubeDataInitializer initializer;
    
    /**
     * 초기 데이터 적재
     * POST /admin/youtube/init
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initVideos() {
        CompletableFuture.runAsync(() -> initializer.initializeVideoData());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "YouTube 영상 데이터 초기화가 시작되었습니다.");
        response.put("status", "processing");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 재초기화
     * POST /admin/youtube/reinit
     */
    @PostMapping("/reinit")
    public ResponseEntity<Map<String, String>> reinitVideos() {
        CompletableFuture.runAsync(() -> initializer.reinitializeVideoData());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "YouTube 영상 데이터 재초기화가 시작되었습니다.");
        response.put("status", "processing");
        
        return ResponseEntity.ok(response);
    }
}
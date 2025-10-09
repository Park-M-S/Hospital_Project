package com.hospital.initializer;

import org.springframework.stereotype.Component;

import com.hospital.service.YouTubeVideoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class YouTubeDataInitializer {
    
    private final YouTubeVideoService youTubeVideoService;
    
    /**
     * YouTube 영상 데이터 초기화
     * 검색어 매핑만 담당, 실제 로직은 Service가 처리
     */
    public void initializeVideoData() {
        log.info("========================================");
        log.info("YouTube 영상 데이터 초기화 시작");
        log.info("========================================");
        
        int totalCount = 0;
        
        // 1. 일반 건강정보
        log.info("[일반 건강정보] 수집 시작");
        totalCount += youTubeVideoService.fetchAndSaveVideos("건강정보", 50, "GENERAL");
        sleep(1000);
        totalCount += youTubeVideoService.fetchAndSaveVideos("의학정보", 50, "GENERAL");
        sleep(1000);
        
        // 2. 주요 진료과별
        log.info("[진료과별] 수집 시작");
        String[] departments = {
            "내과", "외과", "정형외과", "신경외과", "성형외과",
            "피부과", "비뇨기과", "산부인과", "소아청소년과", "안과"
        };
        
        for (String dept : departments) {
            log.info("[{}] 수집 시작", dept);
            totalCount += youTubeVideoService.fetchAndSaveVideos(dept + " 질환", 25, "DEPT_" + dept);
            sleep(1000);
            totalCount += youTubeVideoService.fetchAndSaveVideos(dept + " 치료", 25, "DEPT_" + dept);
            sleep(1000);
        }
        
        // 3. 증상별
        log.info("[증상별] 수집 시작");
        String[] symptoms = {"두통", "요통", "복통", "관절통", "어지러움"};
        
        for (String symptom : symptoms) {
            log.info("[{}] 수집 시작", symptom);
            totalCount += youTubeVideoService.fetchAndSaveVideos(symptom + " 원인", 10, "SYMPTOM");
            sleep(1000);
            totalCount += youTubeVideoService.fetchAndSaveVideos(symptom + " 치료", 10, "SYMPTOM");
            sleep(1000);
        }
        
        // 4. 계절별
        log.info("[계절별] 수집 시작");
        totalCount += youTubeVideoService.fetchAndSaveVideos("환절기 건강관리", 25, "SEASONAL");
        sleep(1000);
        totalCount += youTubeVideoService.fetchAndSaveVideos("겨울철 건강", 25, "SEASONAL");
        sleep(1000);
        totalCount += youTubeVideoService.fetchAndSaveVideos("독감 예방", 25, "SEASONAL");
        
        log.info("========================================");
        log.info("YouTube 영상 데이터 초기화 완료: 총 {}개", totalCount);
        log.info("========================================");
    }
    
    /**
     * 재초기화 (기존 데이터 삭제 후 재수집)
     */
    public void reinitializeVideoData() {
        log.info("========================================");
        log.info("YouTube 영상 데이터 재초기화 시작");
        log.info("========================================");
        
        youTubeVideoService.deleteAllVideos();
        initializeVideoData();
    }
    
    /**
     * API 호출 제한 방지를 위한 대기
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep 인터럽트 발생");
        }
    }
}
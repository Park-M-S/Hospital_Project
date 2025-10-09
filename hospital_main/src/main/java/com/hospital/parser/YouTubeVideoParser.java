package com.hospital.parser;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.YouTubeApiItem;
import com.hospital.dto.YouTubeApiResponse;
import com.hospital.entity.YouTubeVideo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class YouTubeVideoParser {
    
    private final ObjectMapper objectMapper;
    
    /**
     * YouTube API 응답을 YouTubeVideo 엔티티 리스트로 파싱
     * @param jsonResponse YouTube API JSON 응답 문자열
     * @param category 카테고리 (GENERAL, DEPT_내과 등)
     * @param searchQuery 검색에 사용된 쿼리
     * @return YouTubeVideo 엔티티 리스트
     */
    public List<YouTubeVideo> parseToEntities(String jsonResponse, String category, String searchQuery) {
        List<YouTubeVideo> videos = new ArrayList<>();
        
        try {
            // JSON 문자열 -> DTO 변환
            YouTubeApiResponse response = objectMapper.readValue(jsonResponse, YouTubeApiResponse.class);
            
            if (response.getItems() == null || response.getItems().isEmpty()) {
                log.warn("검색 결과 없음");
                return videos;
            }
            
            // 각 아이템을 엔티티로 변환
            for (YouTubeApiItem item : response.getItems()) {
                try {
                    YouTubeVideo video = parseToEntity(item, category, searchQuery);
                    if (video != null) {
                        videos.add(video);
                    }
                } catch (Exception e) {
                    log.error("개별 영상 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("{}개 영상 파싱 완료", videos.size());
            
        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", e.getMessage(), e);
        }
        
        return videos;
    }
    
    /**
     * YouTubeApiItem DTO를 YouTubeVideo 엔티티로 변환
     */
    private YouTubeVideo parseToEntity(YouTubeApiItem item, String category, String searchQuery) {
        // videoId 추출
        if (item.getId() == null || item.getId().getVideoId() == null || item.getId().getVideoId().isEmpty()) {
            log.warn("videoId 없음");
            return null;
        }
        
        String videoId = item.getId().getVideoId();
        YouTubeApiItem.Snippet snippet = item.getSnippet();
        
        if (snippet == null) {
            log.warn("snippet 없음: videoId={}", videoId);
            return null;
        }
        
        return YouTubeVideo.builder()
                .videoId(videoId)
                .title(snippet.getTitle() != null ? snippet.getTitle() : "")
                .description(snippet.getDescription() != null ? snippet.getDescription() : "")
                .thumbnailDefaultUrl(getThumbnailUrl(snippet, "default"))
                .thumbnailMediumUrl(getThumbnailUrl(snippet, "medium"))
                .thumbnailHighUrl(getThumbnailUrl(snippet, "high"))
                .channelId(snippet.getChannelId())
                .channelTitle(snippet.getChannelTitle())
                .publishedAt(parsePublishedAt(snippet.getPublishedAt()))
                .category(category)
                .searchQuery(searchQuery)
                .build();
    }
    
    /**
     * 썸네일 URL 추출
     */
    private String getThumbnailUrl(YouTubeApiItem.Snippet snippet, String size) {
        if (snippet.getThumbnails() == null) {
            return null;
        }
        
        YouTubeApiItem.ThumbnailInfo thumbnail = switch (size) {
            case "default" -> snippet.getThumbnails().getDefaultThumbnail();
            case "medium" -> snippet.getThumbnails().getMedium();
            case "high" -> snippet.getThumbnails().getHigh();
            default -> null;
        };
        
        return thumbnail != null ? thumbnail.getUrl() : null;
    }
    
    /**
     * YouTube API 날짜 형식을 LocalDateTime으로 변환
     * "2023-09-12T14:03:19Z" -> LocalDateTime
     */
    private java.time.LocalDateTime parsePublishedAt(String publishedAt) {
        if (publishedAt == null || publishedAt.isEmpty()) {
            return null;
        }
        
        try {
            // ZonedDateTime으로 파싱 후 LocalDateTime으로 변환
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(publishedAt);
            return zonedDateTime.toLocalDateTime();
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", publishedAt);
            return null;
        }
    }
}
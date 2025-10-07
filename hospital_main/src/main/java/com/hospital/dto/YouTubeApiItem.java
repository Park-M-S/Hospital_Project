package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * YouTube Search API 개별 검색 결과 아이템 (알맹이)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouTubeApiItem {
    
    @JsonProperty("kind")
    private String kind;
    
    @JsonProperty("etag")
    private String etag;
    
    @JsonProperty("id")
    private VideoId id;
    
    @JsonProperty("snippet")
    private Snippet snippet;
    
    /**
     * 비디오 ID 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoId {
        @JsonProperty("kind")
        private String kind;
        
        @JsonProperty("videoId")
        private String videoId;
    }
    
    /**
     * 비디오 상세 정보 (제목, 설명, 썸네일 등)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snippet {
        @JsonProperty("publishedAt")
        private String publishedAt;
        
        @JsonProperty("channelId")
        private String channelId;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("thumbnails")
        private Thumbnails thumbnails;
        
        @JsonProperty("channelTitle")
        private String channelTitle;
        
        @JsonProperty("liveBroadcastContent")
        private String liveBroadcastContent;
        
        @JsonProperty("publishTime")
        private String publishTime;
    }
    
    /**
     * 썸네일 이미지들
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thumbnails {
        @JsonProperty("default")
        private ThumbnailInfo defaultThumbnail;
        
        @JsonProperty("medium")
        private ThumbnailInfo medium;
        
        @JsonProperty("high")
        private ThumbnailInfo high;
    }
    
    /**
     * 개별 썸네일 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThumbnailInfo {
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("width")
        private int width;
        
        @JsonProperty("height")
        private int height;
    }
}
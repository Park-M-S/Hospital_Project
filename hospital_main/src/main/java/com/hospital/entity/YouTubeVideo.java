package com.hospital.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "youtube_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
public class YouTubeVideo {
    
    @Id
    @Column(name = "video_id", nullable = false, length = 50)
    private String videoId;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "thumbnail_default_url", length = 500)
    private String thumbnailDefaultUrl;
    
    @Column(name = "thumbnail_medium_url", length = 500)
    private String thumbnailMediumUrl;
    
    @Column(name = "thumbnail_high_url", length = 500)
    private String thumbnailHighUrl;
    
    @Column(name = "channel_id", length = 100)
    private String channelId;
    
    @Column(name = "channel_title", length = 200)
    private String channelTitle;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Column(name = "search_query", length = 200)
    private String searchQuery;
}
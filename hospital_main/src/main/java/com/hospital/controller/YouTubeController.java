package com.hospital.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.dto.YouTubeApiItem;
import com.hospital.service.YouTubeService;

@RestController
@RequestMapping("/youtube")
public class YouTubeController {
	private final YouTubeService youTubeService;
    
    public YouTubeController(YouTubeService youTubeService) {
    	this.youTubeService = youTubeService;
    }
    
    @GetMapping(value = "/videoData", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<YouTubeApiItem> searchVideos(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "maxResults", defaultValue = "10") int maxResults) {
        
        return youTubeService.searchYouTubeData(query, maxResults);
    }
}
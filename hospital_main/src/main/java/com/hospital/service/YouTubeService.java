package com.hospital.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.caller.YouTubeApiCaller;
import com.hospital.dto.YouTubeApiItem;
import com.hospital.dto.YouTubeApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class YouTubeService {
	private final YouTubeApiCaller youTubeApiCaller;
	private final ObjectMapper objectMapper;

	public YouTubeService(YouTubeApiCaller youTubeApiCaller, ObjectMapper objectMapper) {
		this.youTubeApiCaller = youTubeApiCaller;
		this.objectMapper = objectMapper;

	}

	public List<YouTubeApiItem> searchYouTubeData(String query, int maxResult) {

		try {
			String youTubeData = youTubeApiCaller.searchMedicalVideos(query, maxResult);

			YouTubeApiResponse response = objectMapper.readValue(youTubeData, YouTubeApiResponse.class);

			return response.getItems() != null ? response.getItems() : Collections.emptyList();
		} catch (Exception e) {
			log.error("youTube 데이터 파싱 실패: query = {}, maxResult = {}", query, maxResult, e);
			return Collections.emptyList();
		}

	}
}

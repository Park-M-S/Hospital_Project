package com.hospital.caller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class YouTubeApiCaller {
    
	@Value("${youTube.api.base-url}")
	private String baseUrl;

	@Value("${youTube.api.key}")
	private String serviceKey;

	@Value("${youTube.api.trusted-channels}")
	private String trustedChannelsString;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 신뢰할 수 있는 의료 채널 ID (프로퍼티에서 로드)
    private Set<String> trustedChannels;
    
    public YouTubeApiCaller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        // 프로퍼티에서 신뢰 채널 ID 로드
        this.trustedChannels = new HashSet<>(Arrays.asList(trustedChannelsString.split(",")));
        log.info("신뢰 채널 {} 개 로드 완료", trustedChannels.size());
    }
    
    // 의료 동영상 검색 (신뢰 채널에서만)
    public String searchMedicalVideos(String query, int maxResults) {
        // 입력 검증
        if (maxResults <= 0 || maxResults > 50) {
            log.warn("maxResults는 1-50 사이여야 합니다. 입력값: {}", maxResults);
            maxResults = Math.min(Math.max(maxResults, 1), 50);
        }

        if (query == null || query.trim().isEmpty()) {
            log.info("검색어 없음 - 최신 영상 조회, maxResults: {}", maxResults);
        } else {
            log.info("의료 동영상 검색 시작 - 검색어: {}, maxResults: {}", query, maxResults);
        }

        // 신뢰 채널에서만 검색
        return searchFromTrustedChannels(query, maxResults);
    }
    
    // 신뢰 채널에서만 검색
    private String searchFromTrustedChannels(String query, int maxResults) {
        List<JsonNode> allResults = new ArrayList<>();
        int perChannel = Math.max(1, maxResults / trustedChannels.size());

        log.debug("신뢰 채널({})에서 검색 시작", trustedChannels.size());

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                    .queryParam("part", "snippet")
                    // .queryParam("channelId", channelId)  // 특정 채널 제외 시 주석 유지
                    .queryParam("maxResults", perChannel)
                    .queryParam("type", "video")
                    .queryParam("order", "date")
                    .queryParam("key", serviceKey);

            // 쿼리가 있을 때만 추가
            if (query != null && !query.trim().isEmpty()) {
                builder.queryParam("q", query);
            }

            // URL 생성
            String url = builder.build(false).toUriString();
            log.info("YouTube API 요청 URL: {}", url);

            // 요청 실행
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            // 결과 누적
            if (items != null && items.isArray()) {
                items.forEach(allResults::add);
                log.debug("검색 결과 {}개 추가됨", items.size());
            } else {
                log.warn("items가 null이거나 배열이 아님");
            }

        } catch (Exception e) {
            log.error("YouTube API 요청 중 오류 발생: {}", e.getMessage(), e);
        }

        // 결과 반환
        if (allResults.isEmpty()) {
            log.info("신뢰 채널에서 검색 결과 없음");
            return "{}";
        }

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("items", allResults.subList(0, Math.min(allResults.size(), maxResults)));
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("검색 결과 JSON 변환 실패: {}", e.getMessage(), e);
            return "{}";
        }
    }
}
package com.hospital.caller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class YouTubeApiCaller {
    
	@Value("${youTube.api.base-url}")
	private String baseUrl;

	@Value("${youTube.api.key}")
	private String serviceKey;

    private final RestTemplate restTemplate;
    
    public YouTubeApiCaller(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * YouTube API로 의료 동영상 검색
     * @param query 검색어
     * @param maxResults 최대 결과 수 (1-50)
     * @return JSON 응답 문자열
     */
    public String searchMedicalVideos(String query, int maxResults) {
        // 입력 검증
        if (maxResults <= 0 || maxResults > 50) {
            log.warn("maxResults는 1-50 사이여야 합니다. 입력값: {} -> 조정됨", maxResults);
            maxResults = Math.min(Math.max(maxResults, 1), 50);
        }

        if (query == null || query.trim().isEmpty()) {
            log.warn("검색어가 비어있습니다.");
            return "{}";
        }

        log.info("YouTube 검색 시작 - 검색어: {}, maxResults: {}", query, maxResults);

        try {
            // URL 생성
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                    .queryParam("part", "snippet")
                    .queryParam("q", query)
                    .queryParam("maxResults", maxResults)
                    .queryParam("type", "video")
                    .queryParam("videoDuration", "medium") 
                    .queryParam("order", "relevance")  // 관련도순 정렬
                    .queryParam("key", serviceKey);

            String url = builder.build(false).toUriString();
            log.debug("YouTube API 요청 URL: {}", url);

            // API 호출
            String response = restTemplate.getForObject(url, String.class);
            
            log.info("YouTube API 호출 성공 - 검색어: {}", query);
            return response;

        } catch (Exception e) {
            log.error("YouTube API 호출 실패 - 검색어: {}, 오류: {}", query, e.getMessage(), e);
            return "{}";
        }
    }
}
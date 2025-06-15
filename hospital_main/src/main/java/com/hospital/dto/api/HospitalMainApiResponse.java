package com.hospital.dto.api; 

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // DTO에 정의되지 않은 필드는 무시
public class HospitalMainApiResponse {

    @JsonProperty("response")
    private Response response;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @JsonProperty("header")
        private Header header;
        @JsonProperty("body")
        private Body body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode; // API 응답 결과 코드
        @JsonProperty("resultMsg")
        private String resultMsg;  // API 응답 결과 메시지
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("items")
        private ApiItemsWrapper items; // 실제 데이터 리스트를 감싸는 부분

        @JsonProperty("numOfRows")
        private int numOfRows;     // 페이지당 항목 수
        @JsonProperty("pageNo")
        private int pageNo;        // 현재 페이지 번호
        @JsonProperty("totalCount")
        private int totalCount;    // 전체 항목 수
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiItemsWrapper {
       
        @JsonProperty("item")
        private List<HospitalMainApiItem> item; 
    }
}
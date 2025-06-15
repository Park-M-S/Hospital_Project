package com.hospital.dto.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProDocApiResponse {

    private Response response;

    // ✅ 최상위 response 객체
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Body body;
    }

    // ✅ response.body
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private ApiItemsWrapper items;
        private int totalCount; // 전체 데이터 수 (페이징 등 활용 가능)
    }

    // ✅ body.items (진짜 데이터 배열이 들어있는 곳)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiItemsWrapper {

        /**
         * ✔️ JSON 응답에 단일 객체가 넘어올 경우에도 배열로 파싱되도록 설정
         * 예: { item: { ... } } → [ {...} ]
         */
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<ProDocApiItem> item;
    }
}

package com.hospital.dto.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // 응답 중 사용하지 않는 필드는 무시
public class MedicalSubjectApiResponse {

    // 전체 응답 객체의 최상위 필드
    private Response response;

    /**
     * 응답 전체 구조
     * {
     *   "response": {
     *     "header": { ... },
     *     "body": {
     *       "items": {
     *         "item": [ {...}, {...} ]
     *       }
     *     }
     *   }
     * }
     */
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    // 응답의 상태를 담고 있는 부분 (성공/실패 여부)
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode; // "00"이면 성공
        private String resultMsg;  // 예: "NORMAL SERVICE"
    }

    // 실제 데이터가 들어 있는 본문 영역
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
    }

    // 아이템 목록 래퍼 객체
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
    	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<MedicalSubjectApiItem> item; // 여러 진료과목 정보 리스트
    }
}

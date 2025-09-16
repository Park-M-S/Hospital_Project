package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * ✅ 전문의 정보 API (getSpcSbjtSdrInfo2.7)의 각 아이템 단위 JSON을 매핑하는 DTO 클래스
 * 예시 JSON 항목:
 * {
 *   "ykiho": "병원코드",
 *   "dgsbjtCdNm": "진료과목명",
 *   "dtlSdrCnt": "전문의 수"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // JSON 중 필요한 필드 외 무시
public class ProDocApiItem {

    @JsonProperty("ykiho") // 병원 고유 코드
    private String hospitalCode;

    @JsonProperty("dgsbjtCdNm") // 진료 과목명 (ex. 내과, 신경과 등)
    private String subjectName;

    @JsonProperty("dtlSdrCnt")  // 전문의 수 (해당 과목에 소속된)
    private Integer proDocCount;
}

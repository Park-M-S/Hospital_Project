package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // JSON 중 필요한 필드 외 무시
public class ProDocApiItem {


    @JsonProperty("dgsbjtCdNm") // 진료 과목명 (ex. 내과, 신경과 등)
    private String dgsbjtCdNm;
    

    @JsonProperty("dtlSdrCnt")  // 전문의 수 (해당 과목에 소속된)
    private Integer dtlSdrCnt;
}
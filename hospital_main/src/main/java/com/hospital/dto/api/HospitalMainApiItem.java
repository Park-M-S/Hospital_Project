package com.hospital.dto.api; 

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // DTO에 정의되지 않은 필드는 무시
public class HospitalMainApiItem {

    @JsonProperty("ykiho")
    private String ykiho; // 요양기관기호 (API 필드명)

    @JsonProperty("yadmNm")
    private String yadmNm; // 요양기관명 (API 필드명)

    @JsonProperty("sidoCdNm")
    private String sidoCdNm; // 시도명 (API 필드명)

    @JsonProperty("sgguCdNm")
    private String sgguCdNm; // 시군구명 (API 필드명)

    @JsonProperty("addr")
    private String addr; // 주소 (API 필드명)

    @JsonProperty("telno")
    private String telno; // 전화번호 (API 필드명)

    @JsonProperty("hospUrl")
    private String hospUrl; // 홈페이지 (API 필드명)

    @JsonProperty("drTotCnt")
    private int drTotCnt; // 의사총수 (API 필드명)

    @JsonProperty("XPos")
    private double xPos; // X좌표 (API 필드명)

    @JsonProperty("YPos")
    private double yPos; // Y좌표 (API 필드명)

    
}
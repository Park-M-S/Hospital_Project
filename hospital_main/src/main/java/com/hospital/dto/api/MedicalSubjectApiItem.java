package com.hospital.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalSubjectApiItem {
    private String dgsbjtCd;     // 진료과목 코드
    private String dgsbjtCdNm;   // 진료과목 이름
    private String ykiho;        // 병원 코드
}

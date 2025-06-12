package com.hospital.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmergencyResponse {
    // 기관 기본 정보
    private String dutyName;   // 기관명 (병원 이름)
    private String dutyTel3;   // 응급실 전화번호
    private String hpid;       // 기관 코드 (병원 식별용 ID)

    // 병상 수 현황
    private int hvec;          // 일반 병상 수
    private int hvoc;          // 수술실 병상 수 (기타)
    private int hvcc;          // 중환자실 - 신경과 병상 수
    private int hvncc;         // 중환자실 - 신생아 병상 수
    private int hvicc;         // 중환자실 - 일반 병상 수
    private int hvgc;          // 입원실 - 일반 병상 수

    // 응급실 격리 병상 수
    private int hv29;          // 응급실 음압 격리 병상 수
    private int hv30;          // 응급실 일반 격리 병상 수

    // 중환자실 및 특수 병상 현황
    private int hv31;          // 응급전용 중환자실 병상 수
    private int hv32;          // 중환자실 - 소아 병상 수
    private int hv33;          // 응급전용 소아중환자실 병상 수
    private int hv35;          // 중환자실 - 음압 격리 병상 수
    private int hv36;          // 응급전용 입원실 병상 수
    private int hv38;          // 입원실 - 외상전용 병상 수
    private int hv40;          // 입원실 - 정신과 폐쇄 병동 병상 수
    private int hv41;          // 입원실 - 음압 격리 병상 수

    
    private boolean hvamyn;        // 구급차 가용 여부

    // 데이터 입력 시각 (최근 갱신일)
    private String hvidate;        // 정보 입력 일시 (데이터 기준 시점)

}
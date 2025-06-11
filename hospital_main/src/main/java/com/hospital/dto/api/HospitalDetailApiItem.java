package com.hospital.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 외부 '의료 상세 정보 API'의 JSON 응답 개별 항목(Item)을 매핑하는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class HospitalDetailApiItem {
	
    @JsonProperty("emyDayYn")
    private String emyDayYn; // 주간 응급 진료 가능 여부
    
    @JsonProperty("emyNgtYn")
    private String emyNgtYn; // 야간 응급 진료 가능 여부
    
    @JsonProperty("parkQty")
    private String parkQty; // 주차 가능 대수
    
    @JsonProperty("parkXpnsYn")
    private String parkXpnsYn; // 주차비 유료 여부 (Y/N)
    
    @JsonProperty("lunchWeek")
    private String lunchWeek; // 점심시간
    
    @JsonProperty("rcvWeek")
    private String rcvWeek; // 평일 접수 시간
    
    @JsonProperty("rcvSat")
    private String rcvSat; // 토요일 접수 시간
    
    // 평일 진료시간
    @JsonProperty("trmtMonStart")
    private String trmtMonStart; // 월요일 진료 시작 시간
    
    @JsonProperty("trmtMonEnd")
    private String trmtMonEnd; // 월요일 진료 종료 시간
    
    @JsonProperty("trmtTueStart")
    private String trmtTueStart; // 화요일 진료 시작 시간
    
    @JsonProperty("trmtTueEnd")
    private String trmtTueEnd; // 화요일 진료 종료 시간
    
    @JsonProperty("trmtWedStart")
    private String trmtWedStart; // 수요일 진료 시작 시간
    
    @JsonProperty("trmtWedEnd")
    private String trmtWedEnd; // 수요일 진료 종료 시간
    
    @JsonProperty("trmtThuStart")
    private String trmtThuStart; // 목요일 진료 시작 시간
    
    @JsonProperty("trmtThuEnd")
    private String trmtThuEnd; // 목요일 진료 종료 시간
    
    @JsonProperty("trmtFriStart")
    private String trmtFriStart; // 금요일 진료 시작 시간
    
    @JsonProperty("trmtFriEnd")
    private String trmtFriEnd; // 금요일 진료 종료 시간
    
    
    @JsonProperty("trmtSatStart")
    private String trmtSatStart; // 토요일 진료 시작 시간
    
    @JsonProperty("trmtSatEnd")
    private String trmtSatEnd; // 토요일 진료 종료 시간
    
    @JsonProperty("trmtSunStart")
    private String trmtSunStart; // 일요일 진료 시작 시간
    
    @JsonProperty("trmtSunEnd")
    private String trmtSunEnd; // 일요일 진료 종료 시간
}
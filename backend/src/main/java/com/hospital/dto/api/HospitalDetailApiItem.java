package com.hospital.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 외부 '의료 상세 정보 API'의 JSON 응답 개별 항목(Item)을 매핑하는 DTO
 * (고객님이 제공하신 XML 태그명을 JSON 필드명으로 가정)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HospitalDetailApiItem {

    // 외부 API의 JSON 필드 이름이 'ykiho'라고 가정
    @JsonProperty("ykiho")
    private String ykiho; // 외부 API에서 넘어오는 고유 코드

    @JsonProperty("emyDayYn")
    private String emyDayYn; // 주간 응급 진료 가능 여부

    @JsonProperty("emyNightYn")
    private String emyNightYn; // 야간 응급 진료 가능 여부

    @JsonProperty("parkQty")
    private String parkQty; // 주차 가능 대수

    @JsonProperty("lunchWeek")
    private String lunchWeek; // 점심시간

    @JsonProperty("rcvWeek")
    private String rcvWeek; // 평일 접수 시간

    @JsonProperty("rcvSat")
    private String rcvSat; // 토요일 접수 시간

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

    @JsonProperty("trmtThurStart")
    private String trmtThurStart; // 목요일 진료 시작 시간

    @JsonProperty("trmtThurEnd")
    private String trmtThurEnd; // 목요일 진료 종료 시간

    @JsonProperty("trmtFriStart")
    private String trmtFriStart; // 금요일 진료 시작 시간

    @JsonProperty("trmtFriEnd")
    private String trmtFriEnd; // 금요일 진료 종료 시간

    // 기타 외부 API 필드가 있다면 여기에 추가
}
package com.hospital.emergency.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmergencyRoomApiItem { // 이 클래스는 <item> 태그의 내용을 매핑합니다.

    @JacksonXmlProperty(localName = "cnt") // <cnt> 태그 매핑
    private Integer cnt; // 정수 값이므로 Integer

    @JacksonXmlProperty(localName = "distance") // <distance> 태그 매핑
    private Double distance; // 실수 값이므로 Double

    @JacksonXmlProperty(localName = "dutyAddr") // <dutyAddr> 태그 매핑
    private String dutyAddr;

    @JacksonXmlProperty(localName = "dutyDiv") // <dutyDiv> 태그 매핑
    private String dutyDiv;

    @JacksonXmlProperty(localName = "dutyDivName") // <dutyDivName> 태그 매핑
    private String dutyDivName;

    @JacksonXmlProperty(localName = "dutyName") // <dutyName> 태그 매핑
    private String dutyName;

    @JacksonXmlProperty(localName = "dutyTel1") // <dutyTel1> 태그 매핑
    private String dutyTel1;

    // endTime은 시간 정보이므로 String으로 받는 것이 안전
    @JacksonXmlProperty(localName = "endTime") // <endTime> 태그 매핑
    private String endTime;

    @JacksonXmlProperty(localName = "hpid") // <hpid> 태그 매핑
    private String hpid;

    @JacksonXmlProperty(localName = "latitude") // <latitude> 태그 매핑
    private Double latitude; // 위도이므로 Double

    @JacksonXmlProperty(localName = "longitude") // <longitude> 태그 매핑
    private Double longitude; // 경도이므로 Double

    @JacksonXmlProperty(localName = "rnum") // <rnum> 태그 매핑
    private Integer rnum; // 순번이므로 Integer

    // startTime은 시간 정보이므로 String으로 받는 것이 안전
    @JacksonXmlProperty(localName = "startTime") // <startTime> 태그 매핑
    private String startTime;
}
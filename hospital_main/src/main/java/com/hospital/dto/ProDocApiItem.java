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

    @JsonProperty("ykiho") // 병원 고유 코드
    private String hospitalCode;
    
    @JsonProperty("mdeptGdrCnt")
    private String mdeptGdrCnt; // 의과 일반의 인원수
    
    @JsonProperty("mdeptIntnCnt")
    private String mdeptIntnCnt; // 의과 인턴 인원수
    
    @JsonProperty("mdeptResdntCnt")
    private String mdeptResdntCnt; // 의과 레지던트 인원수
    
    
    @JsonProperty("mdeptSdrCnt")
    private String mdeptSdrCnt; // 의과 전문의 인원수
    
    @JsonProperty("detyGdrCnt")
    private String detyGdrCnt; // 치과 일반의 인원수
    
    @JsonProperty("detyIntnCnt")
    private String detyIntnCnt; // 치과 인턴 인원수
    
    @JsonProperty("detyResdntCnt")
    private String detyResdntCnt; // 치과 레지던트 인원수
    
    @JsonProperty("detySdrCnt")
    private String detySdrCnt; // 치과 전문의 인원수
    
    @JsonProperty("cmdcGdrCnt")
    private String cmdcGdrCnt; // 한방 일반의 인원수
    
    @JsonProperty("cmdcIntnCnt")
    private String cmdcIntnCnt; // 한방 인턴 인원수
    
    @JsonProperty("cmdcResdntCnt")
    private String cmdcResdntCnt; // 한방 레지던트 인원수
    
    @JsonProperty("cmdcSdrCnt")
    private String cmdcSdrCnt; // 한방 전문의 인원수
    
    @JsonProperty("pnursCnt")
    private String pnursCnt; // 조산사 인원수
}

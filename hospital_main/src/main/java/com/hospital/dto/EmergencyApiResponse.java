package com.hospital.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmergencyApiResponse {
	
	@JsonProperty("dutyName")
	private String dutyName; // 기관명

	@JsonProperty("dutyTel3")
	private String dutyTel3; // 응급실 전화번호

	@JsonProperty("hpid")
	private String hpid; // 기관 코드

	@JsonProperty("hvidate")
	private String lastUpdatedDate; // 입력일시

	// === 핵심 병상 현황 ===
	@JsonProperty("hvec")
	private Integer emergencyBeds; // 응급실 일반 병상 (음수=포화)

	@JsonProperty("hvoc")
	private Integer operatingBeds; // 수술실 병상

	@JsonProperty("hvgc")
	private Integer generalWardBeds; // 일반 입원실 병상

	// === 장비/서비스 가용성 ===
	@JsonProperty("hvamyn")
	private Boolean ambulanceAvailability; // 구급차 가용 여부

	@JsonProperty("hvventiayn")
	private Boolean ventilatorAvailability; // 인공호흡기 가용

	@JsonProperty("hvctayn")
	private Boolean ctAvailability; // CT 가용

	@JsonProperty("hvmriayn")
	private Boolean mriAvailability; // MRI 가용

	@JsonProperty("hvcrrtayn")
	private Boolean crrtAvailability; // CRRT(투석) 가용

    
    
    // === 좌표 정보 ===
    private Double coordinateX;     // x 좌표 (위도)
    private Double coordinateY;     // y 좌표 (경도)
    
    private String emergencyAddress; // 주소
    
    
 // 좌표 설정 메서드
 	public void setCoordinates(Double coordinateX, Double coordinateY) {
 		this.coordinateX = coordinateX;
 		this.coordinateY = coordinateY;
 	}
   
}


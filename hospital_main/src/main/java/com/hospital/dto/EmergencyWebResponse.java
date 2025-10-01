package com.hospital.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class EmergencyWebResponse {

	// === 필수 기본 정보 ===
	@JsonProperty("dutyName")
	private String dutyName; // 기관명

	@JsonProperty("dutyTel3")
	private String dutyTel3; // 응급실 전화번호

	@JsonProperty("hpid")
	private String hpid; // 기관 코드

	@JsonProperty("hvidate")
	private String lastUpdatedDate; // 입력일시

	
	@JsonProperty("hvamyn")
	private Boolean ambulanceAvailability; // 구급차 가용 여부

	// === 좌표 정보 (DB에서 추가) ===
	private Double coordinateX; // x 좌표 (위도)
	private Double coordinateY; // y 좌표 (경도)

	private String emergencyAddress;
	
	// === 장비 가용 정보 리스트 ===
	public List<String> availableEquipment;
	
	
	// === 핵심 병상 현황 ===
	public Map<String, Integer> availableBeds;
	
	public static EmergencyWebResponse from(EmergencyApiResponse api) {
	   List<String> availableEquipment = availableEquipment(api);
	   Map<String, Integer> availableBeds = availableBeds(api);
	    
	    return EmergencyWebResponse.builder()
	        .dutyName(api.getDutyName())
	        .dutyTel3(api.getDutyTel3())
	        .hpid(api.getHpid())
	        .lastUpdatedDate(api.getLastUpdatedDate())
	        .availableBeds(availableBeds)
	        .availableEquipment(availableEquipment)
	        .ambulanceAvailability(api.getAmbulanceAvailability())
	        .coordinateX(api.getCoordinateX())
	        .coordinateY(api.getCoordinateY())
	        .emergencyAddress(api.getEmergencyAddress())
	        .build();
	}

	// 장비 추출 로직 분리
	private static List<String> availableEquipment(EmergencyApiResponse api) {
	    Map<String, Boolean> equipmentMap = new LinkedHashMap<>();
	    equipmentMap.put("인공호흡기", api.getVentilatorAvailability());
	    equipmentMap.put("CT", api.getCtAvailability());
	    equipmentMap.put("MRI", api.getMriAvailability());
	    equipmentMap.put("CRRT", api.getCrrtAvailability());
	    
	    return equipmentMap.entrySet().stream()
	        .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
	        .map(Map.Entry::getKey)
	        .collect(Collectors.toList());
	}
	
	private static Map<String, Integer> availableBeds(EmergencyApiResponse api){
		Map <String, Integer> BedsMap = new LinkedHashMap<>();
		BedsMap.put("응급실 일반 병상", api.getEmergencyBeds());
		BedsMap.put("수술실 병상", api.getOperatingBeds());
		BedsMap.put("일반 입원실 병상", api.getGeneralWardBeds());
		
		return BedsMap;
	}
	
}
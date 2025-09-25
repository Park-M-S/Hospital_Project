package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalSubjectApiItem {
	
	@JsonProperty("ykiho") // 병원 고유 코드
    private String hospitalCode;
	
	
	
}

package com.hospital.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hospital.entity.Hospital;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HospitalMainResponse {
	    @JsonProperty("ykiho") // JSON 필드명과 매핑
	    private String hospitalCode;

	    @JsonProperty("yadmNm")
	    private String hospitalName;

	    @JsonProperty("sidoCdNm")
	    private String provinceName;

	    @JsonProperty("sgguCdNm")
	    private String districtName;

	    @JsonProperty("addr")
	    private String hospitalAddress;

	    @JsonProperty("telno")
	    private String hospitalTel;

	    @JsonProperty("hospUrl")
	    private String hospitalHomepage;

	    @JsonProperty("drTotCnt")
	    private int doctorNum;

	    @JsonProperty("XPos")
	    private double XPos; // API 스펙과 일치하는 필드명 유지

	    @JsonProperty("YPos")
	    private double YPos; // API 스펙과 일치하는 필드명 유지
	    public static HospitalMainResponse from(Hospital hospital) {
	    	HospitalMainResponse response = new HospitalMainResponse();
	    	response.setHospitalCode(hospital.getHospitalCode());
	    	response.setHospitalName(hospital.getHospitalName());
	    	 response.setProvinceName(hospital.getProvinceName());
	         response.setDistrictName(hospital.getDistrictName());
	         response.setHospitalAddress(hospital.getHospitalAddress());
	         response.setHospitalTel(hospital.getHospitalTel());
	         response.setHospitalHomepage(hospital.getHospitalHomepage());
	         response.setDoctorNum(hospital.getDoctorNum());
	         response.setXPos(hospital.getCoordinateX()); // 엔티티의 coordinateX를 DTO의 XPos에 매핑
	         response.setYPos(hospital.getCoordinateY()); // 엔티티의 coordinateY를 DTO의 YPos에 매핑
	         return response;
	    }
}


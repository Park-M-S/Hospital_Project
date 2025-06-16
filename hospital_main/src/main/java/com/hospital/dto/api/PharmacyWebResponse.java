package com.hospital.dto.api;

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
public class PharmacyWebResponse {
	
	private String pharmacyName;
	private String pharmacyAddress;
	private double coordinateX;
	private double coordinateY;
	private String PharmacyTel;
	

}

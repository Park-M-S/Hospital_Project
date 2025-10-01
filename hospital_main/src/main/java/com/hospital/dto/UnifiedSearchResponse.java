package com.hospital.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnifiedSearchResponse {
	private List<HospitalWebResponse> hospitals;
	private List<PharmacyWebResponse> pharmacies;
	private List<EmergencyWebResponse> emergencies;

}

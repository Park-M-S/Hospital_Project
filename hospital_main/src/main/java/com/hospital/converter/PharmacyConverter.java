package com.hospital.converter;

import com.hospital.dto.web.PharmacyResponse;
import com.hospital.entity.Pharmacy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PharmacyConverter {

	
	//Entity -> DTO 변환
	public PharmacyResponse toResponse(Pharmacy pharmacy) {
		if (pharmacy == null) {
			return null;
		}

		return PharmacyResponse.builder().pharmacyName(pharmacy.getName()).pharmacyAddress(pharmacy.getAddress())
				.coordinateX(pharmacy.getLongitude()) // 경도 = X좌표
				.coordinateY(pharmacy.getLatitude()) // 위도 = Y좌표
				.PharmacyTel(pharmacy.getPhone()).build();
	}

	//Entity List -> DTO List 변환
	public List<PharmacyResponse> toResponseList(List<Pharmacy> pharmacies) {
		if (pharmacies == null) {
			return null;
		}

		return pharmacies.stream().map(this::toResponse).collect(Collectors.toList());
	}
}

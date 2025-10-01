package com.hospital.converter;

import com.hospital.dto.PharmacyWebResponse;
import com.hospital.entity.Pharmacy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PharmacyConverter {

	
	//Entity -> DTO 변환
	public PharmacyWebResponse convertToDTO(Pharmacy pharmacy) {
		if (pharmacy == null) {
			return null;
		}

		return PharmacyWebResponse.builder().pharmacyName(pharmacy.getName()).pharmacyAddress(pharmacy.getAddress())
				.coordinateX(pharmacy.getLongitude()) // 경도 = X좌표
				.coordinateY(pharmacy.getLatitude()) // 위도 = Y좌표
				.PharmacyTel(pharmacy.getPhone()).build();
	}

	//Entity List -> DTO List 변환
	public List<PharmacyWebResponse> toResponseList(List<Pharmacy> pharmacies) {
		if (pharmacies == null) {
			return null;
		}

		return pharmacies.stream().map(this::convertToDTO).collect(Collectors.toList());
	}
}

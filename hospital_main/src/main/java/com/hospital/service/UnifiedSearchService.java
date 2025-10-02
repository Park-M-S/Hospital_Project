package com.hospital.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.converter.HospitalConverter;
import com.hospital.converter.PharmacyConverter;
import com.hospital.dto.EmergencyWebResponse;
import com.hospital.dto.HospitalWebResponse;
import com.hospital.dto.PharmacyWebResponse;
import com.hospital.dto.UnifiedSearchResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.entity.Pharmacy;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.repository.PharmacyApiRepository;

@Service
public class UnifiedSearchService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final PharmacyApiRepository pharmacyApiRepository;
	private final HospitalConverter hospitalConverter;
	private final PharmacyConverter pharmacyConverter;
	private final EmergencyMockService emergencyMockService;
	
	@Autowired
	public UnifiedSearchService(HospitalMainApiRepository hospitalMainApiRepository,
			PharmacyApiRepository pharmacyApiRepository, HospitalConverter hospitalConverter,
			PharmacyConverter pharmacyConverter, EmergencyMockService emergencyMockService) {

		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.pharmacyApiRepository = pharmacyApiRepository;
		this.hospitalConverter = hospitalConverter;
		this.pharmacyConverter = pharmacyConverter;
		this.emergencyMockService = emergencyMockService;
	}

	@Transactional(readOnly = true)
	public List<UnifiedSearchResponse> search(String searchName) {

		String input = searchName.replace(" ", "");

		if (input.length() < 3) {
			return Collections.emptyList();
		}

		List<HospitalMain> hospitals = hospitalMainApiRepository.findHospitalsByName(searchName);
		List<UnifiedSearchResponse> hospitalsResult = hospitals.stream()
				.map(hospitalConverter::convertToDTO)
				.map(UnifiedSearchResponse::fromHospital)
				.collect(Collectors.toList());

		List<Pharmacy> pharmacies = pharmacyApiRepository.findPharmacyByName(searchName);
		List<UnifiedSearchResponse> pharmaciesResult = pharmacies.stream()
				.map(pharmacyConverter::convertToDTO)
				.map(UnifiedSearchResponse::fromPharmacy)
				.collect(Collectors.toList());

		List<UnifiedSearchResponse> emergencies = emergencyMockService.getMockDataDirect().stream()
				.filter(e -> {
					String dutyNameClean = e.getDutyName().replace(" ", "");
					return dutyNameClean.contains(input);
				})
				.map(UnifiedSearchResponse::fromEmergency)
				.collect(Collectors.toList());

		List<UnifiedSearchResponse> result = new java.util.ArrayList<>();
		result.addAll(hospitalsResult);
		result.addAll(pharmaciesResult);
		result.addAll(emergencies);

		return result;

	}
}

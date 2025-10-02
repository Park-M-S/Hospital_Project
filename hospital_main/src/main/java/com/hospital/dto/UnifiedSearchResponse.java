package com.hospital.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
@JsonSerialize(using = UnifiedSearchResponseSerializer.class)
public class UnifiedSearchResponse {
	// 공통 필드
	private String medicalType; // "HOSPITAL", "PHARMACY", "EMERGENCY"
	private String name;
	private String address;
	private String tel;
	private Double coordinateX;
	private Double coordinateY;

	// 병원 전용 필드
	private String provinceName;
	private String districtName;
	private String hospitalHomepage;
	private Integer totalDoctors;
	private Boolean emergencyDayAvailable;
	private Boolean emergencyNightAvailable;
	private String weekdayLunch;
	private Integer parkingCapacity;
	private Boolean parkingFee;
	private String todayOpen;
	private String todayClose;
	private String noTrmtHoli;
	private List<String> medicalSubjects;
	private Map<String, Integer> professionalDoctors;

	// 응급실 전용 필드
	private String hpid;
	private String lastUpdatedDate;
	private Boolean ambulanceAvailability;
	private List<String> availableEquipment;
	private Map<String, Integer> availableBeds;

	// 변환 메서드
	public static UnifiedSearchResponse fromHospital(HospitalWebResponse hospital) {
		return UnifiedSearchResponse.builder()
				.medicalType("HOSPITAL")
				.name(hospital.getHospitalName())
				.address(hospital.getHospitalAddress())
				.tel(hospital.getHospitalTel())
				.coordinateX(hospital.getCoordinateX())
				.coordinateY(hospital.getCoordinateY())
				.provinceName(hospital.getProvinceName())
				.districtName(hospital.getDistrictName())
				.hospitalHomepage(hospital.getHospitalHomepage())
				.totalDoctors(hospital.getTotalDoctors())
				.emergencyDayAvailable(hospital.getEmergencyDayAvailable())
				.emergencyNightAvailable(hospital.getEmergencyNightAvailable())
				.weekdayLunch(hospital.getWeekdayLunch())
				.parkingCapacity(hospital.getParkingCapacity())
				.parkingFee(hospital.getParkingFee())
				.todayOpen(hospital.getTodayOpen())
				.todayClose(hospital.getTodayClose())
				.noTrmtHoli(hospital.getNoTrmtHoli())
				.medicalSubjects(hospital.getMedicalSubjects())
				.professionalDoctors(hospital.getProfessionalDoctors())
				.build();
	}

	public static UnifiedSearchResponse fromPharmacy(PharmacyWebResponse pharmacy) {
		return UnifiedSearchResponse.builder()
				.medicalType("PHARMACY")
				.name(pharmacy.getPharmacyName())
				.address(pharmacy.getPharmacyAddress())
				.tel(pharmacy.getPharmacyTel())
				.coordinateX(pharmacy.getCoordinateX())
				.coordinateY(pharmacy.getCoordinateY())
				.build();
	}

	public static UnifiedSearchResponse fromEmergency(EmergencyWebResponse emergency) {
		return UnifiedSearchResponse.builder()
				.medicalType("EMERGENCY")
				.name(emergency.getDutyName())
				.address(emergency.getEmergencyAddress())
				.tel(emergency.getDutyTel3())
				.coordinateX(emergency.getCoordinateX())
				.coordinateY(emergency.getCoordinateY())
				.hpid(emergency.getHpid())
				.lastUpdatedDate(emergency.getLastUpdatedDate())
				.ambulanceAvailability(emergency.getAmbulanceAvailability())
				.availableEquipment(emergency.getAvailableEquipment())
				.availableBeds(emergency.getAvailableBeds())
				.build();
	}
}

package com.hospital.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hospital.serializer.UnifiedSearchResponseSerializer;

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
	private String hospitalCode;


	private Integer totalDoctors;

	private String weekdayLunch;
	private Integer parkingCapacity;
	private Boolean parkingFee;
	private String todayOpen;
	private String todayClose;
	private Map<String, Map<String, String>> weeklySchedule;
	private String noTrmtHoli;
	private String noTrmtSun;
	private List<String> medicalSubjects;
	private Map<String, Integer> professionalDoctors;

	// 약국 전용 필드
	private String pharmacyCode;

	// 응급실 전용 필드
	private String hpid;
	private String hvidate;
	private Boolean hvamyn;
	private List<String> availableEquipment;
	private Map<String, Integer> availableBeds;

	// 변환 메서드
	public static UnifiedSearchResponse fromHospital(HospitalWebResponse hospital) {
		return UnifiedSearchResponse.builder().medicalType("HOSPITAL").hospitalCode(hospital.getHospitalCode())
				.name(hospital.getHospitalName()).address(hospital.getHospitalAddress()).tel(hospital.getHospitalTel())
				.coordinateX(hospital.getCoordinateX()).coordinateY(hospital.getCoordinateY())

				.totalDoctors(hospital.getTotalDoctors())

				.weekdayLunch(hospital.getWeekdayLunch()).parkingCapacity(hospital.getParkingCapacity())
				.parkingFee(hospital.getParkingFee()).todayOpen(hospital.getTodayOpen())
				.todayClose(hospital.getTodayClose()).weeklySchedule(hospital.getWeeklySchedule()).noTrmtHoli(hospital.getNoTrmtHoli()).noTrmtSun(hospital.getNoTrmtSun())
				.medicalSubjects(hospital.getMedicalSubjects()).professionalDoctors(hospital.getProfessionalDoctors())
				.build();
	}

	public static UnifiedSearchResponse fromPharmacy(PharmacyWebResponse pharmacy) {
		return UnifiedSearchResponse.builder().medicalType("PHARMACY").pharmacyCode(pharmacy.getPharmacyCode())
				.name(pharmacy.getPharmacyName()).address(pharmacy.getPharmacyAddress()).tel(pharmacy.getPharmacyTel())
				.coordinateX(pharmacy.getCoordinateX()).coordinateY(pharmacy.getCoordinateY()).build();
	}

	public static UnifiedSearchResponse fromEmergency(EmergencyWebResponse emergency) {
		return UnifiedSearchResponse.builder().medicalType("EMERGENCY").name(emergency.getDutyName())
				.address(emergency.getEmergencyAddress()).tel(emergency.getDutyTel3())
				.coordinateX(emergency.getCoordinateX()).coordinateY(emergency.getCoordinateY())
				.hpid(emergency.getHpid()).hvidate(emergency.getHvidate()).hvamyn(emergency.getHvamyn())
				.availableEquipment(emergency.getAvailableEquipment()).availableBeds(emergency.getAvailableBeds())
				.build();
	}
}

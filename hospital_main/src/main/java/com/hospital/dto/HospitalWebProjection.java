package com.hospital.dto;

public interface HospitalWebProjection {
	String getHospitalCode();

	String getHospitalName();

	String getHospitalAddress();

	String getProvinceName();

	String getDistrictName();

	String getHospitalTel();

	String getHospitalHomepage();

	String getTotalDoctors();

	Double getCoordinateX();

	Double getCoordinateY();

	Boolean getEmergencyDayAvailable();

	Boolean getEmergencyNightAvailable();

	String getWeekdayLunch();

	Integer getParkingCapacity();

	Boolean getParkingFee();

	String getTodayOpen();

	String getTodayClose();

	String getNoTrmtHoli();
}

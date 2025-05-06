package com.hospital.dto;

public class HospitalDTO {
	private String hospital_name;
    private String medical_subject;
    private String hospital_address;
    private double coordinateX;
    private double coordinateY;

    public HospitalDTO() {}

    public HospitalDTO(String hospital_name, String medical_subject,String hospital_address, double coordinateX, double coordinateY) {
    	this.hospital_name = hospital_name;
    	this.medical_subject = medical_subject;
    	this.hospital_address = hospital_address;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
    }
    public String getHospitalAddress() {
        return hospital_address;
    }

    public void setHospitalAddress(String hospital_address) {
        this.hospital_address = hospital_address;
    }
    public String getHospitalName() {
        return hospital_name;
    }

    public void setHospitalName(String hospital_name) {
        this.hospital_name = hospital_name;
    }

    public String getSubject() {
        return medical_subject;
    }

    public void setSubject(String medical_subject) {
        this.medical_subject = medical_subject;
    }

    public double getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(double coordinateX) {
        this.coordinateX = coordinateX;
    }

    public double getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(double coordinateY) {
        this.coordinateY = coordinateY;
    }
}

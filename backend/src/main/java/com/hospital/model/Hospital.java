package com.hospital.model;

public class Hospital {
    private String hospitalName;
    private String hospitalTel;
	private String hospitalAddress;
    private double coordinateX;
    private double coordinateY;
    private int doctorNum;
    
    public String getHospitalTel() {
		return hospitalTel;
	}

	public void setHospitalTel(String hospitalTel) {
		this.hospitalTel = hospitalTel;
	}

	public String getHospitalAddress() {
		return hospitalAddress;
	}

	public void setHospitalAddress(String hospitalAddress) {
		this.hospitalAddress = hospitalAddress;
	}

	public int getDoctorNum() {
		return doctorNum;
	}

	public void setDoctorNum(int doctorNum) {
		this.doctorNum = doctorNum;
	}


    public String getHospitalName() {
		return hospitalName;
	}

	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
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

	public Hospital(String hospitalName, String hospitalAddress, String hospitalTel, int doctorNum, double coordinateX, double coordinateY) {
        this.hospitalName = hospitalName;
        this.hospitalAddress = hospitalAddress;
        this.hospitalTel = hospitalTel;
        this.doctorNum = doctorNum;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
    }


}

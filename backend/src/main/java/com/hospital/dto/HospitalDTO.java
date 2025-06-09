package com.hospital.dto;



public class HospitalDTO {
	private String hospital_name;
    private String medical_subject;
    private String hospital_address;
    private double coordinateX;
    private double coordinateY;
    private String emergency_available;
    private Integer park_available;
    private Integer pro_doc;

    public HospitalDTO() {}

    public HospitalDTO(String hospital_name, String medical_subject,String hospital_address, double coordinateX, 
    		double coordinateY, String emergency_available, Integer park_available, Integer pro_doc) {
    	this.hospital_name = hospital_name;
    	this.medical_subject = medical_subject;
    	this.hospital_address = hospital_address;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
        this.emergency_available = emergency_available;
        this.park_available = park_available;
        this.pro_doc = pro_doc;
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
    public String getEmergency_available() {
        return emergency_available;
    }

    public void setEmergency_available(String emergency_available) {
        this.emergency_available = emergency_available;
    }
    public int getPark_available() {
        return park_available;
    }

    public void setPark_available(Integer park_available) {
        this.park_available = park_available;
    }
    
    public int getPro_doc() {
    	return pro_doc;
    }
    
    public void setPro_doc(Integer pro_doc) {
    	this.pro_doc = pro_doc;
    }
    
}

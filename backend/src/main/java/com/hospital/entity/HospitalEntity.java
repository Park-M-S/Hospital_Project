package com.hospital.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Table;


@Table(name = "hospital_main")
public class HospitalEntity {

    @Id
    @Column(name = "hospital_code")
    private String hospitalCode; // 병원 고유 코드
    
    @Column(name = "subject_name")
    private String hospitalSubject; // 병원 고유 코드

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "hospital_address")
    private String hospitalAddress;

    @Column(name = "coordinate_x")
    private double coordinateX;

    @Column(name = "coordinate_y")
    private double coordinateY;

    @Column(name = "emergency_available")
    private String emergencyAvailable;

    @Column(name = "park_available")
    private Integer parkAvailable;
    
    @Column(name = "pro_doc")
    private Integer proDoc;

    // 생성자
    public HospitalEntity() {}

    // getter, setter
    public String getHospitalCode() {
        return hospitalCode;
    }

    public void setHospitalCode(String hospitalCode) {
        this.hospitalCode = hospitalCode;
    }
    
    public String getHospitalSubject() {
        return hospitalSubject;
    }

    public void setHospitalSubject(String hospitalSubject) {
        this.hospitalSubject = hospitalSubject;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public void setHospitalAddress(String hospitalAddress) {
        this.hospitalAddress = hospitalAddress;
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

    public String getEmergencyAvailable() {
        return emergencyAvailable;
    }

    public void setEmergencyAvailable(String emergencyAvailable) {
        this.emergencyAvailable = emergencyAvailable;
    }

    public Integer getParkAvailable() {
        return parkAvailable;
    }

    public void setParkAvailable(Integer parkAvailable) {
        this.parkAvailable = parkAvailable;
    }
    
    public Integer getProDoc() {
    	return proDoc;
    }
    
    public void setProDoc(Integer proDoc) {
    	this.proDoc = proDoc;    
    }

    // 도메인 로직: 응급실 가능 여부 확인
    public boolean matchesTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return true; // 태그가 없으면 필터링 안 함
        }

        for (String tag : tags) {
            if ("응급실".equals(tag) && !"Y".equals(this.emergencyAvailable)) {
                return false; // 응급실 필터링 조건에 안 맞으면 제외
            }
            if ("주차가능".equals(tag) && (this.parkAvailable == null || this.parkAvailable == 0)) {
                return false; // 주차 가능 조건에 안 맞으면 제외
            }
            if ("전문의".equals(tag) && (this.proDoc == null || this.proDoc == 0)) {
            	return false;
            }
        }

        return true; // 모든 태그 조건을 만족하면 포함
    }

}
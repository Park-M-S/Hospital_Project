package com.hospital.dto.web;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalResponseDTO {
    // 기본 정보
    private String hospitalCode;
    private String hospitalName;
    private String hospitalAddress;
    private String provinceName;
    private String districtName;
    private String hospitalTel;
    private String hospitalHomepage;
    private Integer doctorNum;
    
    // 좌표 정보
    private Double coordinateX;
    private Double coordinateY;
    
    // 운영 정보
    private String emergencyDayAvailable;    // Y/N
    private String emergencyNightAvailable;  // Y/N
    private String weekdayLunch;
    private Integer parkingCapacity;
    private String parkingFee;
    private String weekdayReception;
    private String saturdayReception;
    
    // 요일별 운영시간
    private String mondayOpen;
    private String mondayClose;
    private String tuesdayOpen;
    private String tuesdayClose;
    private String wednesdayOpen;
    private String wednesdayClose;
    private String thursdayOpen;
    private String thursdayClose;
    private String fridayOpen;
    private String fridayClose;
    private String saturdayOpen;
    private String saturdayClose;
    private String sundayOpen;
    private String sundayClose;
    
    private String medicalSubject;
    
    // 전문의 정보 (JSON 문자열로 저장)
    private String professionalDoctors;  // "안과:1|이비인후과:1|비뇨의학과:1" 형태
}



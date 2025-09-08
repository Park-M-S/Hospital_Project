package com.hospital.dto;



import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class HospitalWebResponse {
    // 기본 정보
    
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
    private Boolean emergencyDayAvailable;    // Y/N
    private Boolean emergencyNightAvailable;  // Y/N
    private String weekdayLunch;
    private Integer parkingCapacity;
    private Boolean parkingFee;
   
    private String todayOpen;
    private String todayClose;
    
    private List<String> medicalSubject;
    
  
    private Map<String, Integer> professionalDoctors; 
}



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
    
	private String hospitalCode;
    private String hospitalName;
    private String hospitalAddress;

    private String hospitalTel;

    private Integer totalDoctors;
    
    // 좌표 정보
    private Double coordinateX;
    private Double coordinateY;
   
    // 운영 정보
    private String weekdayLunch;
    private Integer parkingCapacity;
    private Boolean parkingFee;
   
    private String todayOpen;
    private String todayClose;
    
    private String noTrmtHoli;
    private String noTrmtSun;
    
    private Map<String, Map<String, String>> weeklySchedule;
    
    private List<String> medicalSubjects;
    
  
    private Map<String, Integer> professionalDoctors; 
    
    
    
}



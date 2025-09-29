package com.hospital.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Getter
public class RegionConfig {
    
    @Value("${hospital.region.sido.code}")
    private String sidoCode;
    
    @Value("${hospital.region.sido.name}")
    private String sidoName;
    
    @Value("${hospital.region.city.name}")
    private String cityName;
    
    @Value("${hospital.region.sigungu.codes}")
    private String sigunguCodesString;
    
    @Value("${hospital.region.sigungu.names}")
    private String sigunguNamesString;
    
    
    @Value("#{'${hospital.region.emergency.city.names}'.split(',')}")
    private List<String> emergencyCityNames;
    
    @Value("${hospital.region.nationwide.sido.codes}")
    private String nationwideSidoCodesString;
    
    @Value("${hospital.region.nationwide.sido.names}")
    private String nationwideSidoNamesString;
    
    // 편의 메서드들
    public List<String> getSigunguCodes() {
        return Arrays.asList(sigunguCodesString.split(","));
    }
    
    public List<String> getSigunguNames() {
        return Arrays.asList(sigunguNamesString.split(","));
    }
    public List<String> getNationwideSidoCodes() {
        return Arrays.asList(nationwideSidoCodesString.split(","));
    }
    public List<String> getAllNationwideSidoNames() {
        return Arrays.asList(nationwideSidoNamesString.split(","));
    }
    
    
    public String getDistrictName(String sigunguCode) {
        List<String> codes = getSigunguCodes();
        List<String> names = getSigunguNames();
        
        int index = codes.indexOf(sigunguCode);
        if (index >= 0 && index < names.size()) {
            return names.get(index);
        }
        return sigunguCode; // 매핑되지 않으면 코드 그대로 반환
    }
    public String getSidoName(String sidoCode) {
        List<String> codes = getNationwideSidoCodes();
        List<String> names = getAllNationwideSidoNames();
        
        int index = codes.indexOf(sidoCode);
        if (index >= 0 && index < names.size()) {
            return names.get(index);
        }
        return sidoCode; // 매핑 없으면 그대로 반환
    }
}
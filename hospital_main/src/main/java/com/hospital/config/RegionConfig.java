package com.hospital.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
public class RegionConfig {
    
    // ✅ @Value로 properties에서 읽어오기
    @Value("${region.sido.code}")
    private String sidoCode;
    
    @Value("${region.sido.name}")
    private String sidoName;
    
    // 성남시 코드들 (쉼표로 구분된 문자열을 List로 변환)
    @Value("${region.seongnam.codes}")
    private String seongnamCodesStr;
    
    @Value("${region.seongnam.names}")
    private String seongnamNamesStr;
    
    // 좌표 범위
    @Value("${region.coordinate.min.latitude}")
    private double minLatitude;
    
    @Value("${region.coordinate.max.latitude}")
    private double maxLatitude;
    
    @Value("${region.coordinate.min.longitude}")
    private double minLongitude;
    
    @Value("${region.coordinate.max.longitude}")
    private double maxLongitude;
    
    @Value("${region.default}")
    private String defaultRegion;
    
    /**
     * 기본 지역의 시군구 코드들 반환 (성남시)
     */
    public List<String> getDefaultRegionCodes() {
        if ("seongnam".equals(defaultRegion)) {
            return getSeongnamCodes();
        }
        // 나중에 다른 지역 추가 시 여기에 추가
        return getSeongnamCodes(); // 기본값
    }
    
    /**
     * 기본 지역의 시군구 이름들 반환 (성남시)
     */
    public List<String> getDefaultRegionNames() {
        if ("seongnam".equals(defaultRegion)) {
            return getSeongnamNames();
        }
        return getSeongnamNames(); // 기본값
    }
    
    /**
     * 성남시 코드 리스트
     */
    public List<String> getSeongnamCodes() {
        return Arrays.asList(seongnamCodesStr.split(","));
    }
    
    /**
     * 성남시 이름 리스트  
     */
    public List<String> getSeongnamNames() {
        return Arrays.asList(seongnamNamesStr.split(","));
    }
    
    /**
     * 좌표 유효성 검사
     */
    public boolean isValidCoordinate(double latitude, double longitude) {
        return latitude >= minLatitude && latitude <= maxLatitude &&
               longitude >= minLongitude && longitude <= maxLongitude;
    }
    
    /**
     * 시군구 코드를 이름으로 변환 (성남시만)
     */
    public String getDistrictName(String sgguCode) {
        List<String> codes = getSeongnamCodes();
        List<String> names = getSeongnamNames();
        
        int index = codes.indexOf(sgguCode);
        return index >= 0 && index < names.size() ? names.get(index) : sgguCode;
    }
    
    /**
     * 시도 정보
     */
    public SidoInfo getSido() {
        return new SidoInfo(sidoCode, sidoName);
    }
    
    @Data
    public static class SidoInfo {
        private final String code;
        private final String name;
        
        public SidoInfo(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
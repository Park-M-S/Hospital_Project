package com.hospital.dto;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SubjectCodeWrapper {
    
    private static final Map<String, String> SUBJECT_CODE_TO_DEPARTMENT = new HashMap<>();
    
    static {
        // 일반의
        SUBJECT_CODE_TO_DEPARTMENT.put("00", "일반의");
        
        // 의과
        SUBJECT_CODE_TO_DEPARTMENT.put("01", "내과");
        SUBJECT_CODE_TO_DEPARTMENT.put("02", "신경과");
        SUBJECT_CODE_TO_DEPARTMENT.put("03", "정신건강의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("04", "외과");
        SUBJECT_CODE_TO_DEPARTMENT.put("05", "정형외과");
        SUBJECT_CODE_TO_DEPARTMENT.put("06", "신경외과");
        SUBJECT_CODE_TO_DEPARTMENT.put("07", "심장혈관흉부외과");
        SUBJECT_CODE_TO_DEPARTMENT.put("08", "성형외과");
        SUBJECT_CODE_TO_DEPARTMENT.put("09", "마취통증의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("10", "산부인과");
        SUBJECT_CODE_TO_DEPARTMENT.put("11", "소아청소년과");
        SUBJECT_CODE_TO_DEPARTMENT.put("12", "안과");
        SUBJECT_CODE_TO_DEPARTMENT.put("13", "이비인후과");
        SUBJECT_CODE_TO_DEPARTMENT.put("14", "피부과");
        SUBJECT_CODE_TO_DEPARTMENT.put("15", "비뇨의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("16", "영상의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("17", "방사선종양학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("18", "병리과");
        SUBJECT_CODE_TO_DEPARTMENT.put("19", "진단검사의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("20", "결핵과");
        SUBJECT_CODE_TO_DEPARTMENT.put("21", "재활의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("22", "핵의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("23", "가정의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("24", "응급의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("25", "직업환경의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("26", "예방의학과");
        
        // 기타/보건
        SUBJECT_CODE_TO_DEPARTMENT.put("27", "기타1(치과)");
        SUBJECT_CODE_TO_DEPARTMENT.put("28", "기타4(한방)");
        SUBJECT_CODE_TO_DEPARTMENT.put("31", "기타2");
        SUBJECT_CODE_TO_DEPARTMENT.put("40", "기타2(2)");
        SUBJECT_CODE_TO_DEPARTMENT.put("41", "보건");
        SUBJECT_CODE_TO_DEPARTMENT.put("42", "기타3");
        SUBJECT_CODE_TO_DEPARTMENT.put("43", "보건기관치과");
        SUBJECT_CODE_TO_DEPARTMENT.put("44", "보건기관한방");
        
        // 치과
        SUBJECT_CODE_TO_DEPARTMENT.put("49", "치과");
        SUBJECT_CODE_TO_DEPARTMENT.put("50", "구강악안면외과");
        SUBJECT_CODE_TO_DEPARTMENT.put("51", "치과보철과");
        SUBJECT_CODE_TO_DEPARTMENT.put("52", "치과교정과");
        SUBJECT_CODE_TO_DEPARTMENT.put("53", "소아치과");
        SUBJECT_CODE_TO_DEPARTMENT.put("54", "치주과");
        SUBJECT_CODE_TO_DEPARTMENT.put("55", "치과보존과");
        SUBJECT_CODE_TO_DEPARTMENT.put("56", "구강내과");
        SUBJECT_CODE_TO_DEPARTMENT.put("57", "영상치의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("58", "구강병리과");
        SUBJECT_CODE_TO_DEPARTMENT.put("59", "예방치과");
        SUBJECT_CODE_TO_DEPARTMENT.put("60", "치과소계");
        SUBJECT_CODE_TO_DEPARTMENT.put("61", "통합치의학과");
        
        // 한방
        SUBJECT_CODE_TO_DEPARTMENT.put("80", "한방내과");
        SUBJECT_CODE_TO_DEPARTMENT.put("81", "한방부인과");
        SUBJECT_CODE_TO_DEPARTMENT.put("82", "한방소아과");
        SUBJECT_CODE_TO_DEPARTMENT.put("83", "한방안·이비인후·피부과");
        SUBJECT_CODE_TO_DEPARTMENT.put("84", "한방신경정신과");
        SUBJECT_CODE_TO_DEPARTMENT.put("85", "침구과");
        SUBJECT_CODE_TO_DEPARTMENT.put("86", "한방재활의학과");
        SUBJECT_CODE_TO_DEPARTMENT.put("87", "사상체질과");
        SUBJECT_CODE_TO_DEPARTMENT.put("88", "한방응급");
        SUBJECT_CODE_TO_DEPARTMENT.put("89", "한방응급");
        SUBJECT_CODE_TO_DEPARTMENT.put("90", "한방소계");
    }
    
    /**
     * 과목코드를 병원과명으로 변환
     */
    public String getDepartmentName(String subjectCode) {
        return SUBJECT_CODE_TO_DEPARTMENT.getOrDefault(subjectCode, "알 수 없는 과목");
    }
}
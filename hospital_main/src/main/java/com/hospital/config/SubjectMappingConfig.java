package com.hospital.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import jakarta.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Configuration
@PropertySource("classpath:subject.properties")
@Getter
public class SubjectMappingConfig {
    
    @Value("${medical.subject.codes}")
    private String subjectCodes;
    
    @Value("${medical.subject.names}")
    private String subjectNames;
    
    
    //편의 메서드
    public List<String> getSubjectCodes() {
        return Arrays.asList(subjectCodes.split(","));
    }
    
    public List<String> getSubjectNames() {
        return Arrays.asList(subjectNames.split(","));
    }
    
    public String getDepartmentName(String subjectCode) {
        List<String> codes = getSubjectCodes();
        List<String> names = getSubjectNames();

        int index = codes.indexOf(subjectCode);
        if (index >= 0 && index < names.size()) {
            return names.get(index);
        }
        return subjectCode; // 매핑되지 않으면 코드 그대로 반환
    }
   
}
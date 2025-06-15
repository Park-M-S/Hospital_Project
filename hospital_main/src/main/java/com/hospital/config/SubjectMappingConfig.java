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

    @Value("${medical.subject.dental.normalized}")
    private String dentalNormalized;
    
    @Value("${medical.subject.dental.subjects}")
    private String dentalSubjects;

    @Value("${medical.subject.oriental.normalized}")
    private String orientalNormalized;
    
    @Value("${medical.subject.oriental.subjects}")
    private String orientalSubjects;

    @Value("${medical.subject.neurology.normalized}")
    private String neurologyNormalized;
    
    @Value("${medical.subject.neurology.subjects}")
    private String neurologySubjects;

    // 정규화 매핑을 위한 Map (원본과목명 -> 정규화된 과목명)
    private Map<String, String> subjectMappings = new HashMap<>();

    @PostConstruct
    public void initializeMappings() {
        // 치과 과목들 매핑
        Arrays.stream(dentalSubjects.split(","))
                .map(String::trim)
                .forEach(subject -> subjectMappings.put(subject, dentalNormalized));

        // 한의과 과목들 매핑
        Arrays.stream(orientalSubjects.split(","))
                .map(String::trim)
                .forEach(subject -> subjectMappings.put(subject, orientalNormalized));

        // 신경과 과목들 매핑
        Arrays.stream(neurologySubjects.split(","))
                .map(String::trim)
                .forEach(subject -> subjectMappings.put(subject, neurologyNormalized));

        log.info("✅ 진료과목 정규화 매핑 초기화 완료: {}개 과목 등록", subjectMappings.size());
    }

    /**
     * 진료과목명을 정규화하여 반환
     * @param rawSubjectName 원본 진료과목명
     * @return 정규화된 진료과목명 (매핑이 없으면 원본 그대로 반환)
     */
    public String normalizeSubjectName(String rawSubjectName) {
        if (rawSubjectName == null || rawSubjectName.trim().isEmpty()) {
            return "기타";
        }
        
        return subjectMappings.getOrDefault(rawSubjectName.trim(), rawSubjectName.trim());
    }
}
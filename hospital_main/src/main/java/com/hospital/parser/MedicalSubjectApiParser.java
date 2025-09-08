package com.hospital.parser;

import com.hospital.config.SubjectMappingConfig;
import com.hospital.dto.MedicalSubjectApiResponse;
import com.hospital.entity.MedicalSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
public class MedicalSubjectApiParser {

    private final SubjectMappingConfig mappingConfig;

    @Autowired
    public MedicalSubjectApiParser(SubjectMappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

   
    public List<MedicalSubject> parse(MedicalSubjectApiResponse response, String hospitalCode) {
        try {
            // ✅ 응답 구조 유효성 검증
            if (response == null ||
                response.getResponse() == null ||
                response.getResponse().getBody() == null ||
                response.getResponse().getBody().getItems() == null ||
                response.getResponse().getBody().getItems().getItem() == null) {
                
                log.warn("진료과목 API 응답 구조가 예상과 다름 - 빈 리스트 반환: {}", hospitalCode);
                return List.of(); // ← Exception 대신 빈 리스트 반환
            }

            //중복 진료과 제거용 Set
            Set<String> seenSubjects = new HashSet<>();

            return response.getResponse().getBody().getItems().getItem().stream()
                .map(item -> mappingConfig.normalizeSubjectName(item.getDgsbjtCdNm())) // 설정 기반 정규화
                .filter(seenSubjects::add) // 중복 제거: Set에 없던 값만 통과
                .map(subjectName -> {
              
                    return MedicalSubject.builder()
                            .hospitalCode(hospitalCode)
                            .subjectName(subjectName)
                            .build();
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            // 예외 발생 시 로그 남기고 빈 리스트 반환 (다른 파서들과 동일한 패턴)
            log.error("진료과목 파싱 오류 - 빈 리스트 반환: {}", hospitalCode, e);
            return List.of();
        }
    }
}
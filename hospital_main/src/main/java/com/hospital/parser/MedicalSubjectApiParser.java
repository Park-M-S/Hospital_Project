package com.hospital.parser;

import com.hospital.config.SubjectMappingConfig;
import com.hospital.dto.api.MedicalSubjectApiResponse;
import com.hospital.entity.MedicalSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ✅ 진료과목 API 응답을 파싱하고, 정규화 및 중복 제거하여 MedicalSubject 엔티티 리스트로 변환하는 클래스
 */
@Slf4j
@Component
public class MedicalSubjectApiParser {

    private final SubjectMappingConfig mappingConfig;

    @Autowired
    public MedicalSubjectApiParser(SubjectMappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

    /**
     * ✅ API 응답을 파싱하고 정제된 MedicalSubject 리스트 반환
     * @param response JSON 파싱된 응답 객체
     * @param hospitalCode 병원 고유 코드 (YKIHO)
     * @return 중복 제거 및 정규화된 진료과목 리스트
     */
    public List<MedicalSubject> parse(MedicalSubjectApiResponse response, String hospitalCode) throws Exception {
        // ✅ 응답 구조 유효성 검증
        if (response.getResponse() == null ||
            response.getResponse().getBody() == null ||
            response.getResponse().getBody().getItems() == null ||
            response.getResponse().getBody().getItems().getItem() == null) {
            throw new Exception("진료과목 API 응답 구조가 예상과 다릅니다.");
        }

        // ✅ 중복 진료과 제거용 Set
        Set<String> seenSubjects = new HashSet<>();

        return response.getResponse().getBody().getItems().getItem().stream()
            .map(item -> mappingConfig.normalizeSubjectName(item.getDgsbjtCdNm())) // ✅ 설정 기반 정규화
            .filter(seenSubjects::add) // ✅ 중복 제거: Set에 없던 값만 통과
            .map(subjectName -> {
                MedicalSubject subject = new MedicalSubject();
                subject.setHospitalCode(hospitalCode);
                subject.setSubjectName(subjectName);
                return subject;
            })
            .collect(Collectors.toList());
    }
}
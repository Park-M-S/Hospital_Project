package com.hospital.parser;

import com.hospital.config.SubjectMappingConfig;
import com.hospital.dto.api.ProDocApiItem;
import com.hospital.dto.api.ProDocApiResponse;
import com.hospital.entity.ProDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ProDocApiParser {

    private final SubjectMappingConfig mappingConfig;

    @Autowired
    public ProDocApiParser(SubjectMappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

    /**
     * ✅ ProDocApiResponse → ProDoc 리스트로 파싱
     * @param response 공공 API로부터 받은 전문의 데이터 응답
     * @param hospitalCode 병원 코드 (ykiho)
     * @return 정규화된 ProDoc 객체 리스트
     */
    public List<ProDoc> parse(ProDocApiResponse response, String hospitalCode) {
        // 응답 검증
        if (response == null || response.getResponse() == null || response.getResponse().getBody() == null) {
            return List.of(); // 빈 리스트 반환
        }

        List<ProDoc> result = new ArrayList<>();
        List<ProDocApiItem> items = response.getResponse().getBody().getItems().getItem();

        if (items == null) return result;

        for (ProDocApiItem item : items) {
            String rawSubjectName = item.getSubjectName();  // 원본 진료과명
            Integer count = item.getProDocCount();          // 전문의 수

            // ✅ 설정 기반 과목명 정규화
            String normalized = mappingConfig.normalizeSubjectName(rawSubjectName);

            // ✅ 엔티티 매핑
            ProDoc doc = new ProDoc();
            doc.setHospitalCode(hospitalCode);
            doc.setSubjectName(normalized);
            doc.setProDocCount(count != null ? count : 0); // null 방지

            result.add(doc);
        }

        return result;
    }
}
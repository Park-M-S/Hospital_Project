package com.hospital.parser;

import com.hospital.config.SubjectMappingConfig;
import com.hospital.dto.ProDocApiItem;
import com.hospital.dto.ProDocApiResponse;
import com.hospital.entity.ProDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ProDocApiParser {

	private final SubjectMappingConfig SubjectMappingConfig;

	@Autowired
	public ProDocApiParser(SubjectMappingConfig mappingConfig) {
		this.SubjectMappingConfig = mappingConfig;
	}

	public List<ProDoc> parse(ProDocApiResponse response, String hospitalCode) {
		// 응답 검증
		if (response == null || response.getResponse() == null || response.getResponse().getBody() == null) {
			return List.of(); // 빈 리스트 반환
		}

		List<ProDoc> result = new ArrayList<>();
		List<ProDocApiItem> items = response.getResponse().getBody().getItems().getItem();

		if (items == null)
			return result;

		for (ProDocApiItem item : items) {
			String rawSubjectName = item.getSubjectName(); // 원본 진료과명
			Integer count = item.getProDocCount(); // 전문의 수

			// 설정 기반 과목명 정규화
			String normalized = SubjectMappingConfig.normalizeSubjectName(rawSubjectName);

			// Builder 패턴으로 엔티티 생성
			ProDoc doc = ProDoc.builder().hospitalCode(hospitalCode).subjectName(normalized)
					.proDocCount(count != null ? count : 0) // null 방지
					.build();

			result.add(doc);
		}

		return result;
	}
}
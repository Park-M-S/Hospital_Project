
package com.hospital.parser;



import com.hospital.dto.HospitalMainApiItem;
import com.hospital.dto.HospitalMainApiResponse;
import com.hospital.dto.MedicalSubjectApiItem;
import com.hospital.dto.MedicalSubjectApiResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.entity.MedicalSubject;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional; // Optional 임포트
import java.util.stream.Collectors;

@Component
@Slf4j
public class MedicalSubjectApiParser {

	public List<MedicalSubject> parseSubjects(MedicalSubjectApiResponse apiResponseDto, String subjectName) {
	    log.debug("과목 {} 병원 데이터 파싱 시작", subjectName);

	    validateApiResponse(apiResponseDto);

	    List<MedicalSubject> subjects = extractAndConvertItems(apiResponseDto, subjectName);

	    log.debug("과목 {} 병원 데이터 파싱 완료: {}건", subjectName, subjects.size());
	    return subjects;
	}

	private List<MedicalSubject> extractAndConvertItems(MedicalSubjectApiResponse response, String subjectName) {
	    return Optional.ofNullable(response)
	            .map(MedicalSubjectApiResponse::getResponse)
	            .map(MedicalSubjectApiResponse.Response::getBody)
	            .map(MedicalSubjectApiResponse.Body::getItems)
	            .map(MedicalSubjectApiResponse.ApiItemsWrapper::getItem)
	            .orElseGet(ArrayList::new)
	            .stream()
	            .map(itemDto -> convertToSubject(itemDto, subjectName))
	            .filter(Objects::nonNull)
	            .collect(Collectors.toList());
	}

	private MedicalSubject convertToSubject(MedicalSubjectApiItem itemDto, String subjectName) {
	    if (itemDto == null || itemDto.getHospitalCode() == null || itemDto.getHospitalCode().trim().isEmpty()) {
	        log.warn("유효하지 않은 병원 데이터: {}", itemDto);
	        return null;
	    }

	    return MedicalSubject.builder()
	            .hospitalCode(itemDto.getHospitalCode())
	            .subjects(subjectName) // 여기서 과목명을 넣음
	            .build();
	}

    
    private void validateApiResponse(MedicalSubjectApiResponse response) {
        if (response == null || response.getResponse() == null || response.getResponse().getHeader() == null) {
            throw new RuntimeException("API 응답이 올바르지 않습니다");
        }
        
        String resultCode = response.getResponse().getHeader().getResultCode();
        String resultMsg = response.getResponse().getHeader().getResultMsg();
        
        if (!"00".equals(resultCode)) {
            throw new RuntimeException("API 응답 오류: " + resultCode + " - " + resultMsg);
        }
    }
}
package com.hospital.parser;

import com.hospital.dto.ProDocApiItem;
import com.hospital.dto.ProDocApiResponse;
import com.hospital.entity.ProDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProDocApiParser {
    
    public List<ProDoc> parse(ProDocApiResponse apiResponseDto, String hospitalCode) {
        log.debug("병원 {} 전문의 데이터 파싱 시작", hospitalCode);
        validateApiResponse(apiResponseDto);
        List<ProDoc> proDocs = extractAndConvertItems(apiResponseDto, hospitalCode);
        log.debug("병원 {} 전문의 데이터 파싱 완료: {}건", hospitalCode, proDocs.size());
        return proDocs;
    }
    
    private List<ProDoc> extractAndConvertItems(ProDocApiResponse response, String hospitalCode) {
        return Optional.ofNullable(response)
                .map(ProDocApiResponse::getResponse)
                .map(ProDocApiResponse.Response::getBody)
                .map(ProDocApiResponse.Body::getItems)
                .map(ProDocApiResponse.ApiItemsWrapper::getItem)
                .orElseGet(ArrayList::new)
                .stream()
                .map(itemDto -> convertToProDoc(itemDto, hospitalCode))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private ProDoc convertToProDoc(ProDocApiItem itemDto, String hospitalCode) {
        if (itemDto == null) {
            log.warn("ProDocApiItem이 null입니다");
            return null;
        }

        String subjectName = itemDto.getDgsbjtCdNm();
        Integer proDocCount = itemDto.getDtlSdrCnt();


        if (subjectName == null || subjectName.trim().isEmpty()) {
            log.warn("과목명이 유효하지 않음 - 과목명: [{}]", subjectName);
            return null;
        }


        return ProDoc.builder()
                .hospitalCode(hospitalCode)
                .subjectName(subjectName)
                .proDocCount(proDocCount)
                .build();
    }
    
    private void validateApiResponse(ProDocApiResponse response) {
        if (response == null || response.getResponse() == null || response.getResponse().getBody() == null) {
            throw new RuntimeException("API 응답이 올바르지 않습니다");
        }
        
        // 헤더가 없으므로 기본적인 구조 검증만
        log.debug("API 응답 검증 완료");
    }
}
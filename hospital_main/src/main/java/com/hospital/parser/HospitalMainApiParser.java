
package com.hospital.parser;



import com.hospital.dto.api.HospitalMainApiItem;
import com.hospital.dto.api.HospitalMainApiResponse;
import com.hospital.entity.HospitalMain;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional; // Optional 임포트
import java.util.stream.Collectors;

@Component
@Slf4j
public class HospitalMainApiParser {

    public List<HospitalMain> parseHospitals(HospitalMainApiResponse apiResponseDto) {
        log.debug("병원 데이터 파싱 시작");
        
        // 1. 응답 검증
        validateApiResponse(apiResponseDto);
        
        // 2. 아이템 추출 및 변환
        List<HospitalMain> hospitals = extractAndConvertItems(apiResponseDto);
        
        log.debug("병원 데이터 파싱 완료: {}건", hospitals.size());
        return hospitals;
    }

    private void validateApiResponse(HospitalMainApiResponse response) {
        if (response == null || response.getResponse() == null || response.getResponse().getHeader() == null) {
            throw new RuntimeException("API 응답이 올바르지 않습니다");
        }
        
        String resultCode = response.getResponse().getHeader().getResultCode();
        String resultMsg = response.getResponse().getHeader().getResultMsg();
        
        if (!"00".equals(resultCode)) {
            throw new RuntimeException("API 응답 오류: " + resultCode + " - " + resultMsg);
        }
    }

    private List<HospitalMain> extractAndConvertItems(HospitalMainApiResponse response) {
        return Optional.ofNullable(response)
                .map(HospitalMainApiResponse::getResponse)
                .map(HospitalMainApiResponse.Response::getBody)
                .map(HospitalMainApiResponse.Body::getItems)
                .map(HospitalMainApiResponse.ApiItemsWrapper::getItem)
                .orElseGet(ArrayList::new)
                .stream()
                .map(this::convertToHospital)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private HospitalMain convertToHospital(HospitalMainApiItem itemDto) {
        if (itemDto == null || itemDto.getYkiho() == null || itemDto.getYkiho().trim().isEmpty()) {
            log.warn("유효하지 않은 병원 데이터: {}", itemDto);
            return null;
        }
        
        HospitalMain hospital = new HospitalMain();
        hospital.setHospitalCode(itemDto.getYkiho());
        hospital.setHospitalName(itemDto.getYadmNm());
        hospital.setProvinceName(itemDto.getSidoCdNm());
        hospital.setDistrictName(itemDto.getSgguCdNm());
        hospital.setHospitalAddress(itemDto.getAddr());
        hospital.setHospitalTel(itemDto.getTelno());
        hospital.setHospitalHomepage(itemDto.getHospUrl());
        hospital.setDoctorNum(itemDto.getDrTotCnt());
        hospital.setCoordinateX(itemDto.getXPos());
        hospital.setCoordinateY(itemDto.getYPos());
        
        return hospital;
    }
}

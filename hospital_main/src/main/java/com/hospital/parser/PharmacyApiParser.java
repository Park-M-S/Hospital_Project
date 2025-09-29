package com.hospital.parser;

import com.hospital.dto.PharmacyApiItem;
import com.hospital.dto.PharmacyApiResponse;
import com.hospital.entity.Pharmacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PharmacyApiParser {

    /**
     * HospitalMainApiParser.parseHospitals()와 동일한 구조
     */
    public List<Pharmacy> parsePharmacies(PharmacyApiResponse apiResponseDto) {
        log.debug("약국 데이터 파싱 시작");
        
        // 1. 응답 검증
        validateApiResponse(apiResponseDto);
        
        // 2. 아이템 추출 및 변환
        List<Pharmacy> pharmacies = extractAndConvertItems(apiResponseDto);
        
        log.debug("약국 데이터 파싱 완료: {}건", pharmacies.size());
        return pharmacies;
    }

    /**
     * API 응답 검증 - HospitalMainApiParser와 동일한 패턴
     */
    private void validateApiResponse(PharmacyApiResponse response) {
        if (response == null || response.getHeader() == null) {
            throw new RuntimeException("API 응답이 올바르지 않습니다");
        }
        
        String resultCode = response.getHeader().getResultCode();
        String resultMsg = response.getHeader().getResultMsg();
        
        if (!"00".equals(resultCode)) {
            throw new RuntimeException("API 응답 오류: " + resultCode + " - " + resultMsg);
        }
    }

    /**
     * 아이템 추출 및 변환 - HospitalMainApiParser와 동일한 패턴
     */
    private List<Pharmacy> extractAndConvertItems(PharmacyApiResponse response) {
        return Optional.ofNullable(response)
                .map(PharmacyApiResponse::getBody)
                .map(PharmacyApiResponse.Body::getItems)
                .orElseGet(ArrayList::new)
                .stream()
                .map(this::convertToPharmacy)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 단일 아이템을 Pharmacy 엔티티로 변환 - HospitalMainApiParser.convertToHospital()와 동일한 패턴
     */
    private Pharmacy convertToPharmacy(PharmacyApiItem itemDto) {
        if (itemDto == null || itemDto.getYkiho() == null || itemDto.getYkiho().trim().isEmpty()) {
            log.warn("유효하지 않은 약국 데이터: {}", itemDto);
            return null;
        }
        
        return Pharmacy.builder()
                .name(itemDto.getYadmNm())
                .address(itemDto.getAddr())
                .phone(itemDto.getTelno())
                .latitude(itemDto.getYPos())
                .longitude(itemDto.getXPos())
                .ykiho(itemDto.getYkiho())
                .build();
    }

    /**
     * 기존 메서드 - 하위 호환성을 위해 유지
     */
    public List<Pharmacy> parseToEntities(List<PharmacyApiItem> apiItems) {
        if (apiItems == null || apiItems.isEmpty()) {
            return List.of();
        }

        return apiItems.stream()
                .map(this::convertToPharmacy)
                .filter(pharmacy -> pharmacy != null && pharmacy.isValid())
                .collect(Collectors.toList());
    }
}
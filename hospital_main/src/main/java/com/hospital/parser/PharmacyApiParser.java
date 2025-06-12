package com.hospital.parser;

import com.hospital.dto.api.PharmacyApiItem;
import com.hospital.entity.Pharmacy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PharmacyApiParser {

    /**
     * API 응답 아이템들을 Entity로 변환
     */
    public List<Pharmacy> parseToEntities(List<PharmacyApiItem> apiItems) {
        if (apiItems == null || apiItems.isEmpty()) {
            return List.of();
        }

        return apiItems.stream()
                .filter(this::isValidItem)
                .map(this::parseToEntity)
                .collect(Collectors.toList());
    }

    /**
     * 단일 API 아이템을 Entity로 변환
     */
    private Pharmacy parseToEntity(PharmacyApiItem apiItem) {
        return Pharmacy.builder()
                .name(apiItem.getYadmNm())
                .address(apiItem.getAddr())
                .phone(apiItem.getTelno())
                .latitude(apiItem.getYPos())
                .longitude(apiItem.getXPos())
                .ykiho(apiItem.getYkiho())
                .build();
    }

    /**
     * 약국 데이터 유효성 검사
     */
    private boolean isValidItem(PharmacyApiItem item) {
        if (item == null) {
            return false;
        }

        // 필수 필드 검증
        if (isEmptyString(item.getYkiho()) || isEmptyString(item.getYadmNm())) {
            return false;
        }

        // 좌표 유효성 검사 (한국 좌표 범위)
        return item.getYPos() != null && item.getXPos() != null &&
               item.getYPos() >= 33.0 && item.getYPos() <= 43.0 &&
               item.getXPos() >= 124.0 && item.getXPos() <= 132.0;
    }

    private boolean isEmptyString(String str) {
        return str == null || str.trim().isEmpty();
    }
}
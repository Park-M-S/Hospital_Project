package com.hospital.parser;


import com.hospital.dto.api.PharmacyApiItem;
import com.hospital.entity.Pharmacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PharmacyApiParser {

    

	  /**
     * API 응답 아이템들을 Entity로 변환
     */
    public List<Pharmacy> parseToEntities(List<PharmacyApiItem> apiItems) {
        if (apiItems == null || apiItems.isEmpty()) {
            return List.of();
        }

        return apiItems.stream()
                .map(this::parseToEntity)
                .filter(pharmacy -> pharmacy != null && pharmacy.isValid()) //엔티티의 검증 메서드 사용
                .collect(Collectors.toList());
    }

    /**
     * 단일 API 아이템을 Entity로 변환
     */
    private Pharmacy parseToEntity(PharmacyApiItem apiItem) {
        if (apiItem == null) {
            return null;
        }

        return Pharmacy.builder()
                .name(apiItem.getYadmNm())
                .address(apiItem.getAddr())
                .phone(apiItem.getTelno())
                .latitude(apiItem.getYPos())
                .longitude(apiItem.getXPos())
                .ykiho(apiItem.getYkiho())
                .build();
    }
}
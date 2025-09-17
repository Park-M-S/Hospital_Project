package com.hospital.parser;


import com.hospital.dto.ProDocApiItem;
import com.hospital.dto.ProDocApiResponse;
import com.hospital.entity.ProDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProDocApiParser {

    public List<ProDoc> parse(ProDocApiResponse response, String hospitalCode) {
        if (response == null || response.getResponse() == null || 
            response.getResponse().getBody() == null || 
            response.getResponse().getBody().getItems() == null ||
            response.getResponse().getBody().getItems().getItem() == null) {
            return List.of();
        }

        List<ProDocApiItem> items = response.getResponse().getBody().getItems().getItem();
        if (items.isEmpty()) {
            return List.of();
        }

        String subjectDetails = items.stream()
                .map(item -> item.getSubjectName() + "(" + item.getProDocCount() + "명)")
                .collect(Collectors.joining(", "));

        ProDoc record = ProDoc.builder()
                .hospitalCode(hospitalCode)
                .subjectDetails(subjectDetails)
                .build();

        log.debug("병원 {} 전문의 정보 통합: {}", hospitalCode, subjectDetails);

        return List.of(record);
    }
}
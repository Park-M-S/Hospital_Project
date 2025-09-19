package com.hospital.parser;


import com.hospital.dto.MedicalSubjectApiResponse;
import com.hospital.entity.MedicalSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
public class MedicalSubjectApiParser {

    

    @Autowired
    public MedicalSubjectApiParser() {
        
    }

   
    public List<MedicalSubject> parse(MedicalSubjectApiResponse response, String hospitalCode) {
        try {
            if (response == null ||
                response.getResponse() == null ||
                response.getResponse().getBody() == null ||
                response.getResponse().getBody().getItems() == null ||
                response.getResponse().getBody().getItems().getItem() == null) {
                
                log.warn("진료과목 API 응답 구조가 예상과 다름 - 빈 리스트 반환: {}", hospitalCode);
                return List.of();
            }

            // 중복 제거용 Set
            Set<String> seenSubjects = new HashSet<>();

            String subjectStr = response.getResponse().getBody().getItems().getItem().stream()
                    .map(item -> item.getDgsbjtCdNm())
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(",")); // 쉼표로 연결

            MedicalSubject record = MedicalSubject.builder()
                    .hospitalCode(hospitalCode)
                    .subjects(subjectStr) // String 필드에 저장
                    .build();// subjects -> subjectName

            return List.of(record);

        } catch (Exception e) {
            log.error("진료과목 파싱 오류 - 빈 리스트 반환: {}", hospitalCode, e);
            return List.of();
        }
    }
}
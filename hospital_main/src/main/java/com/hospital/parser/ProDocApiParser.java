
package com.hospital.parser;



import com.hospital.dto.HospitalMainApiItem;
import com.hospital.dto.HospitalMainApiResponse;
import com.hospital.dto.ProDocApiItem;
import com.hospital.dto.ProDocApiResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.entity.ProDoc;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional; // Optional 임포트
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProDocApiParser {

    public List<ProDoc> parseProDocs(ProDocApiResponse apiResponseDto) {
        log.debug("ProDoc 데이터 파싱 시작");

        validateApiResponse(apiResponseDto);

        return Optional.ofNullable(apiResponseDto)
                .map(ProDocApiResponse::getResponse)
                .map(ProDocApiResponse.Response::getBody)
                .map(ProDocApiResponse.Body::getItems)   
                .map(ProDocApiResponse.ApiItemsWrapper::getItem)
                .orElseGet(ArrayList::new)
                .stream()
                .map(this::convertToProDoc) // 메서드 이름 명확히
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void validateApiResponse(ProDocApiResponse response) {
        if (response == null || response.getResponse() == null || response.getResponse().getHeader() == null) {
            throw new RuntimeException("API 응답이 올바르지 않습니다");
        }

        String resultCode = response.getResponse().getHeader().getResultCode();
        String resultMsg = response.getResponse().getHeader().getResultMsg();

        if (!"00".equals(resultCode)) {
            throw new RuntimeException("API 응답 오류: " + resultCode + " - " + resultMsg);
        }
    }

    private ProDoc convertToProDoc(ProDocApiItem itemDto) {
        if (itemDto == null || itemDto.getHospitalCode() == null || itemDto.getHospitalCode().trim().isEmpty()) {
            log.warn("유효하지 않은 병원 데이터: {}", itemDto);
            return null;
        }

        return ProDoc.builder()
                .hospitalCode(itemDto.getHospitalCode())
                // 의과
                .medGeneralCnt(itemDto.getMdeptGdrCnt())
                .medInternCnt(itemDto.getMdeptIntnCnt())
                .medResidentCnt(itemDto.getMdeptResdntCnt())
                .medSpecialistCnt(itemDto.getMdeptSdrCnt())
                // 치과
                .dentGeneralCnt(itemDto.getDetyGdrCnt())
                .dentInternCnt(itemDto.getDetyIntnCnt())
                .dentResidentCnt(itemDto.getDetyResdntCnt())
                .dentSpecialistCnt(itemDto.getDetySdrCnt())
                // 한방
                .orientalGeneralCnt(itemDto.getCmdcGdrCnt())
                .orientalInternCnt(itemDto.getCmdcIntnCnt())
                .orientalResidentCnt(itemDto.getCmdcResdntCnt())
                .orientalSpecialistCnt(itemDto.getCmdcSdrCnt())
                // 기타
                .midwifeCnt(itemDto.getPnursCnt())
                .build();
    }
}
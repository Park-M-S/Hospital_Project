package com.hospital.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.api.HospitalDetailApiItem;
import com.hospital.dto.api.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class HospitalDetailApiParser {

    private final ObjectMapper objectMapper;

    public HospitalDetailApiParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * HospitalDetailApiResponse DTO를 받아서 HospitalDetail 엔티티로 변환합니다.
     * 
     * @param apiResponseDto 외부 API로부터 받은 HospitalDetailApiResponse DTO
     * @return 변환된 HospitalDetail 엔티티 (단일 항목이거나 null)
     */
    public HospitalDetail parseHospitalDetail(HospitalDetailApiResponse apiResponseDto) {
        // 실제 API 구조에 맞춘 수정: item이 단일 객체임
        HospitalDetailApiItem itemDto = Optional.ofNullable(apiResponseDto)
                .map(HospitalDetailApiResponse::getBody)
                .map(HospitalDetailApiResponse.Body::getItems)
                .map(HospitalDetailApiResponse.Items::getItem) // 단일 객체 반환
                .orElse(null);

        if (itemDto == null) {
            System.out.println("No item found in HospitalDetail API response.");
            return null;
        }

        // HospitalDetailApiItem의 필드를 HospitalDetail 엔티티의 필드로 매핑
        // 실제 API 응답 필드에 맞춰 수정
        return HospitalDetail.builder()
                .hospitalCode(itemDto.getYkiho()) // 이 필드가 없다면 다른 식별자 사용 필요
                .emyDayYn(itemDto.getEmyDayYn())
                .emyNightYn(itemDto.getEmyNightYn())
                .parkQty(parseInteger(itemDto.getParkQty()))
                .lunchWeek(itemDto.getLunchWeek())
                .rcvWeek(itemDto.getRcvWeek())
                .rcvSat(itemDto.getRcvSat())
                .trmtMonStart(itemDto.getTrmtMonStart())
                .trmtMonEnd(itemDto.getTrmtMonEnd())
                .trmtTueStart(itemDto.getTrmtTueStart())
                .trmtTueEnd(itemDto.getTrmtTueEnd())
                .trmtWedStart(itemDto.getTrmtWedStart())
                .trmtWedEnd(itemDto.getTrmtWedEnd())
                .trmtThurStart(itemDto.getTrmtThurStart())
                .trmtThurEnd(itemDto.getTrmtThurEnd())
                .trmtFriStart(itemDto.getTrmtFriStart())
                .trmtFriEnd(itemDto.getTrmtFriEnd())
                .build();
    }

    /**
     * 문자열을 Integer로 안전하게 변환하는 헬퍼 메서드
     */
    private Integer parseInteger(String str) {
        try {
            return (str != null && !str.trim().isEmpty()) ? Integer.parseInt(str.trim()) : null;
        } catch (NumberFormatException e) {
            System.err.println("Cannot parse '" + str + "' to Integer in HospitalDetailApiParser. Setting to null.");
            return null;
        }
    }
}
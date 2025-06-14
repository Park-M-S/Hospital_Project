package com.hospital.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.api.HospitalDetailApiResponse;
import com.hospital.dto.api.HospitalDetailApiItem;
import com.hospital.entity.HospitalDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HospitalDetailApiParser {

    private final ObjectMapper objectMapper;

    public HospitalDetailApiParser() {
        this.objectMapper = new ObjectMapper();
    }

    // JSON 파싱 중 오류 발생 시 예외를 호출자에게 던짐 -> 즉시 중단 가능
    public HospitalDetailApiResponse parseResponse(String json) throws Exception {
        return objectMapper.readValue(json, HospitalDetailApiResponse.class);
    }

    // 예외를 잡아서 로그 출력 후, 필요에 따라 RuntimeException으로 다시 던져서 호출자도 중단하도록 함
    public List<HospitalDetailApiItem> parseItems(String json) {
        try {
            HospitalDetailApiResponse response = parseResponse(json);
            if (response != null &&
                response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null) {
                JsonNode itemsNode = response.getResponse().getBody().getItems();
                JsonNode itemArrayNode = itemsNode.get("item");

                if (itemArrayNode != null && itemArrayNode.isArray()) {
                    List<HospitalDetailApiItem> result = new ArrayList<>();
                    for (JsonNode itemNode : itemArrayNode) {
                        HospitalDetailApiItem item = objectMapper.treeToValue(itemNode, HospitalDetailApiItem.class);
                        result.add(item);
                    }
                    return result;
                }
            }
        } catch (Exception e) {
            // 오류 내용 확인 가능하게 출력
            log.error("HospitalDetailApiParser parseItems 오류:", e);

            // 필요시 여기서 바로 중단시키기 위해 RuntimeException으로 던질 수 있음
            throw new RuntimeException("HospitalDetailApiParser에서 JSON 파싱 오류 발생", e);
        }
        return Collections.emptyList();
    }

    /**
     * 🔥 수정된 메서드: HospitalDetailAsyncRunner에서 호출하는 parse 메서드 추가
     * HospitalDetailApiResponse와 병원 코드를 받아 엔티티 리스트로 변환해서 반환
     */
    public List<HospitalDetail> parse(HospitalDetailApiResponse response, String hospitalCode) {
        List<HospitalDetail> entities = new ArrayList<>();
        
        try {
            if (response != null &&
                response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null) {
                
                JsonNode itemsNode = response.getResponse().getBody().getItems();
                
                // 🔥 기존 파싱 로직 복원
                if (itemsNode.isArray()) {
                    // items가 직접 배열인 경우
                    for (JsonNode itemNode : itemsNode) {
                        HospitalDetailApiItem item = objectMapper.treeToValue(itemNode, HospitalDetailApiItem.class);
                        HospitalDetail entity = convertDtoToEntity(item, hospitalCode);
                        entities.add(entity);
                    }
                } else {
                    // items 안에 item 배열이 있는 경우
                    JsonNode itemArrayNode = itemsNode.get("item");
                    if (itemArrayNode != null) {
                        if (itemArrayNode.isArray()) {
                            for (JsonNode itemNode : itemArrayNode) {
                                HospitalDetailApiItem item = objectMapper.treeToValue(itemNode, HospitalDetailApiItem.class);
                                HospitalDetail entity = convertDtoToEntity(item, hospitalCode);
                                entities.add(entity);
                            }
                        } else {
                            // 단일 item인 경우
                            HospitalDetailApiItem item = objectMapper.treeToValue(itemArrayNode, HospitalDetailApiItem.class);
                            HospitalDetail entity = convertDtoToEntity(item, hospitalCode);
                            entities.add(entity);
                        }
                    } else {
                        // ← 여기에 빈 Entity 생성 추가
                        log.info("상세 데이터 없음 - 빈 Entity 생성: {}", hospitalCode);
                        HospitalDetail emptyEntity = HospitalDetail.builder()
                                .hospitalCode(hospitalCode)
                                .build();
                        entities.add(emptyEntity);
                    }
                }
            } else {
                // API 응답 자체가 이상할 때도 빈 Entity 생성
                log.info("API 응답 이상 - 빈 Entity 생성: {}", hospitalCode);
                HospitalDetail emptyEntity = HospitalDetail.builder()
                        .hospitalCode(hospitalCode)
                        .build();
                entities.add(emptyEntity);
            }
        } catch (Exception e) {
            log.error("파싱 오류 - 빈 Entity 생성: {}", hospitalCode, e);
            HospitalDetail emptyEntity = HospitalDetail.builder()
                    .hospitalCode(hospitalCode)
                    .build();
            entities.add(emptyEntity);
        }
        
        return entities;
    }
    /**
     * JSON 응답에서 아이템들을 파싱하고,
     * 각 아이템과 병원 코드를 받아 엔티티 리스트로 변환해서 반환
     */
    public List<HospitalDetail> parseToEntities(String json, String hospitalCode) {
        List<HospitalDetailApiItem> items = parseItems(json);
        List<HospitalDetail> entities = new ArrayList<>();

        for (HospitalDetailApiItem item : items) {
            HospitalDetail entity = convertDtoToEntity(item, hospitalCode);
            entities.add(entity);
        }
        return entities;
    }

    /**
     * DTO -> Entity 변환 메서드 (코드 재사용용)
     * 🔥 수정: 안전한 정수 변환 및 추가 필드들 매핑
     */
    private HospitalDetail convertDtoToEntity(HospitalDetailApiItem dto, String hospitalCode) {
        return HospitalDetail.builder()
                .hospitalCode(hospitalCode)
                .emyDayYn(safeGetString(dto.getEmyDayYn()))
                .emyNightYn(safeGetString(dto.getEmyNgtYn())) // 🔥 수정: 메서드명 통일
                .parkQty(parseInteger(dto.getParkQty())) // 🔥 수정: 안전한 정수 변환
                .parkXpnsYn(safeGetString(dto.getParkXpnsYn()))  
                .lunchWeek(safeGetString(dto.getLunchWeek()))
                .rcvWeek(safeGetString(dto.getRcvWeek()))
                .rcvSat(safeGetString(dto.getRcvSat()))
                .trmtMonStart(safeGetString(dto.getTrmtMonStart()))
                .trmtMonEnd(safeGetString(dto.getTrmtMonEnd()))
                .trmtTueStart(safeGetString(dto.getTrmtTueStart()))
                .trmtTueEnd(safeGetString(dto.getTrmtTueEnd()))
                .trmtWedStart(safeGetString(dto.getTrmtWedStart()))
                .trmtWedEnd(safeGetString(dto.getTrmtWedEnd()))
                .trmtThurStart(safeGetString(dto.getTrmtThuStart())) // 🔥 수정: 메서드명 통일
                .trmtThurEnd(safeGetString(dto.getTrmtThuEnd()))   // 🔥 수정: 메서드명 통일
                .trmtFriStart(safeGetString(dto.getTrmtFriStart()))
                .trmtFriEnd(safeGetString(dto.getTrmtFriEnd()))
                .trmtSatStart(safeGetString(dto.getTrmtSatStart()))  // 토요일 진료 시작
                .trmtSatEnd(safeGetString(dto.getTrmtSatEnd()))      // 토요일 진료 종료
                .trmtSunStart(safeGetString(dto.getTrmtSunStart()))  // 일요일 진료 시작
                .trmtSunEnd(safeGetString(dto.getTrmtSunEnd())) 
                .build();
    }

    /**
     * 🔥 추가: 안전한 정수 변환 메서드
     * 문자열을 정수로 변환하되, 오류 발생 시 null 반환
     */
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            log.warn("정수 변환 실패: {}", value);
            return null;
        }
    }

    /**
     * 🔥 추가: 안전한 문자열 변환 메서드
     * null이나 빈 문자열을 안전하게 처리
     */
    private String safeGetString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
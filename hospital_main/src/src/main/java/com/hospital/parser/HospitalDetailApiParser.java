package com.hospital.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.HospitalDetailApiItem;
import com.hospital.dto.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HospitalDetailApiParser {

    private final ObjectMapper objectMapper;

    public HospitalDetailApiParser() {
        this.objectMapper = new ObjectMapper();
    }

    public HospitalDetailApiResponse parseResponse(String json) throws Exception {
        return objectMapper.readValue(json, HospitalDetailApiResponse.class);
    }

    public List<HospitalDetail> parse(HospitalDetailApiResponse response, String hospitalCode) {
        List<HospitalDetail> entities = new ArrayList<>();

        try {
            if (response != null && response.getResponse() != null && response.getResponse().getBody() != null) {
                JsonNode itemsNode = response.getResponse().getBody().getItems();

                if (itemsNode != null) {
                    // items가 배열인 경우
                    if (itemsNode.isArray()) {
                        for (JsonNode itemNode : itemsNode) {
                            processItem(itemNode, hospitalCode, entities);
                        }
                    } else {
                        // items 안에 item 배열이 있는 경우
                        JsonNode itemArrayNode = itemsNode.get("item");
                        if (itemArrayNode != null) {
                            if (itemArrayNode.isArray()) {
                                for (JsonNode itemNode : itemArrayNode) {
                                    processItem(itemNode, hospitalCode, entities);
                                }
                            } else {
                                processItem(itemArrayNode, hospitalCode, entities);
                            }
                        } else {
                            createEmptyEntity(hospitalCode, entities);
                        }
                    }
                } else {
                    createEmptyEntity(hospitalCode, entities);
                }
            } else {
                createEmptyEntity(hospitalCode, entities);
            }
        } catch (Exception e) {
            log.error("파싱 중 예외 발생 - 빈 Entity 생성: {}", hospitalCode, e);
            createEmptyEntity(hospitalCode, entities);
        }

        return entities;
    }

    private void processItem(JsonNode itemNode, String hospitalCode, List<HospitalDetail> entities) throws Exception {
        HospitalDetailApiItem item = objectMapper.treeToValue(itemNode, HospitalDetailApiItem.class);
        HospitalDetail entity = convertDtoToEntity(item, hospitalCode);
        entities.add(entity);
    }

    private void createEmptyEntity(String hospitalCode, List<HospitalDetail> entities) {
        HospitalDetail emptyEntity = HospitalDetail.builder()
                .hospitalCode(hospitalCode)
                .build();
        entities.add(emptyEntity);
    }

    private HospitalDetail convertDtoToEntity(HospitalDetailApiItem dto, String hospitalCode) {
        return HospitalDetail.builder()
                .hospitalCode(hospitalCode)
                .emyDayYn(safeGetString(dto.getEmyDayYn()))
                .emyNightYn(safeGetString(dto.getEmyNgtYn()))
                .parkQty(parseInteger(dto.getParkQty()))
                .parkXpnsYn(safeGetString(dto.getParkXpnsYn()))
                .lunchWeek(safeGetString(dto.getLunchWeek()))
                .rcvWeek(safeGetString(dto.getRcvWeek()))
                .rcvSat(safeGetString(dto.getRcvSat()))
                //.noTrmtHoli(safeGetString(dto.getNoTrmtHoli()))
                //.noTrmtSun(safeGetString(dto.getNoTrmtSun()))
                .trmtMonStart(safeGetString(dto.getTrmtMonStart()))
                .trmtMonEnd(safeGetString(dto.getTrmtMonEnd()))
                .trmtTueStart(safeGetString(dto.getTrmtTueStart()))
                .trmtTueEnd(safeGetString(dto.getTrmtTueEnd()))
                .trmtWedStart(safeGetString(dto.getTrmtWedStart()))
                .trmtWedEnd(safeGetString(dto.getTrmtWedEnd()))
                .trmtThurStart(safeGetString(dto.getTrmtThuStart()))
                .trmtThurEnd(safeGetString(dto.getTrmtThuEnd()))
                .trmtFriStart(safeGetString(dto.getTrmtFriStart()))
                .trmtFriEnd(safeGetString(dto.getTrmtFriEnd()))
                .trmtSatStart(safeGetString(dto.getTrmtSatStart()))
                .trmtSatEnd(safeGetString(dto.getTrmtSatEnd()))
                .trmtSunStart(safeGetString(dto.getTrmtSunStart()))
                .trmtSunEnd(safeGetString(dto.getTrmtSunEnd()))
                .build();
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            log.warn("정수 변환 실패: {}", value);
            return null;
        }
    }

    private String safeGetString(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    private Boolean convertParkingFeeToBoolean(String parkXpnsYn) {
        if (parkXpnsYn == null) return null;
        return "Y".equalsIgnoreCase(parkXpnsYn.trim());
    }
}

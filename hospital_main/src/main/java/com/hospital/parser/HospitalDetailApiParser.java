package com.hospital.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.HospitalDetailApiItem;
import com.hospital.dto.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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

    
  
    public List<HospitalDetail> parse(HospitalDetailApiResponse response, String hospitalCode) {
        List<HospitalDetail> entities = new ArrayList<>();
        
        try {
            if (response != null &&
                response.getResponse() != null &&
                response.getResponse().getBody() != null &&
                response.getResponse().getBody().getItems() != null) {
                
                JsonNode itemsNode = response.getResponse().getBody().getItems();
                
                
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
   

  
    private HospitalDetail convertDtoToEntity(HospitalDetailApiItem dto, String hospitalCode) {
        return HospitalDetail.builder()
                .hospitalCode(hospitalCode)
                .emyDayYn(safeGetString(dto.getEmyDayYn()))
                .emyNightYn(safeGetString(dto.getEmyNgtYn())) 
                .parkQty(parseInteger(dto.getParkQty())) // 
                .parkXpnsYn(safeGetString(dto.getParkXpnsYn()))  
                .lunchWeek(safeGetString(dto.getLunchWeek()))
                .rcvWeek(safeGetString(dto.getRcvWeek()))
                .rcvSat(safeGetString(dto.getRcvSat()))
                .noTrmtHoli(safeGetString(dto.getNoTrmtHoli()))
                .noTrmtSun(safeGetString(dto.getNoTrmtSun()))
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

    
    private String safeGetString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
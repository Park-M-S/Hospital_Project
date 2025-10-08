package com.hospital.parser;

import com.hospital.dto.HospitalDetailApiItem;
import com.hospital.dto.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
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
			if (response != null && response.getResponse() != null && response.getResponse().getBody() != null
					&& response.getResponse().getBody().getItems() != null
					&& response.getResponse().getBody().getItems().getItem() != null) {

				List<HospitalDetailApiItem> items = response.getResponse().getBody().getItems().getItem();

				for (HospitalDetailApiItem item : items) {
					HospitalDetail entity = convertDtoToEntity(item, hospitalCode);
					entities.add(entity);
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

	private void createEmptyEntity(String hospitalCode, List<HospitalDetail> entities) {
		HospitalDetail emptyEntity = HospitalDetail.builder().hospitalCode(hospitalCode).build();
		entities.add(emptyEntity);
	}

	private HospitalDetail convertDtoToEntity(HospitalDetailApiItem dto, String hospitalCode) {
		return HospitalDetail.builder().hospitalCode(hospitalCode)

				.parkQty(parseInteger(dto.getParkQty())).parkXpnsYn(safeGetString(dto.getParkXpnsYn()))
				.lunchWeek(safeGetString(dto.getLunchWeek()))

				.noTrmtHoli(safeGetString(dto.getNoTrmtHoli())).noTrmtSun(safeGetString(dto.getNoTrmtSun()))
				.trmtMonStart(safeGetString(dto.getTrmtMonStart())).trmtMonEnd(safeGetString(dto.getTrmtMonEnd()))
				.trmtTueStart(safeGetString(dto.getTrmtTueStart())).trmtTueEnd(safeGetString(dto.getTrmtTueEnd()))
				.trmtWedStart(safeGetString(dto.getTrmtWedStart())).trmtWedEnd(safeGetString(dto.getTrmtWedEnd()))
				.trmtThurStart(safeGetString(dto.getTrmtThuStart())).trmtThurEnd(safeGetString(dto.getTrmtThuEnd()))
				.trmtFriStart(safeGetString(dto.getTrmtFriStart())).trmtFriEnd(safeGetString(dto.getTrmtFriEnd()))
				.trmtSatStart(safeGetString(dto.getTrmtSatStart())).trmtSatEnd(safeGetString(dto.getTrmtSatEnd()))
				.trmtSunStart(safeGetString(dto.getTrmtSunStart())).trmtSunEnd(safeGetString(dto.getTrmtSunEnd()))
				.build();
	}

	private Integer parseInteger(String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		try {
			return Integer.valueOf(value.trim());
		} catch (NumberFormatException e) {
			log.warn("정수 변환 실패: {}", value);
			return null;
		}
	}

	private String safeGetString(String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		return value.trim();
	}
}
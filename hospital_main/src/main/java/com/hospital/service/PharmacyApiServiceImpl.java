package com.hospital.service;

import com.hospital.caller.PharmacyApiCaller;
import com.hospital.dto.api.OpenApiWrapper;
import com.hospital.dto.api.PharmacyApiItem;
import com.hospital.entity.Pharmacy;
import com.hospital.parser.PharmacyApiParser;
import com.hospital.repository.PharmacyApiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyApiServiceImpl implements PharmacyApiService {

	private final PharmacyApiCaller apiCaller;
	private final PharmacyApiRepository pharmacyApiRepository;
	private final PharmacyApiParser pharmacyApiParser;

	@Override
	@Transactional
	public int fetchAndSaveByDistrict(String sgguCd) {
		log.info("🏥 [{}] 지역 약국 데이터 수집 시작", sgguCd);

		// 1. 기존 데이터 삭제
		pharmacyApiRepository.deleteAll();
		log.info("🗑️ 기존 약국 데이터 전체 삭제 완료");

		// 2. API 호출
		OpenApiWrapper.Body body = apiCaller.callApiByDistrict(sgguCd);

		// 3. 응답 유효성 검사
		if (body == null || body.getItems() == null || body.getItems().isEmpty()) {
			log.warn("📭 [{}] 지역에 저장할 약국 정보 없음", sgguCd);
			return 0;
		}

		List<PharmacyApiItem> apiItems = body.getItems();
		log.info("📦 [{}] 지역 파싱된 약국 수: {}", sgguCd, apiItems.size());

		// 4. Entity 변환 (유효성 검사 포함)
		List<Pharmacy> pharmacies = pharmacyApiParser.parseToEntities(apiItems);

		if (pharmacies.size() != apiItems.size()) {
			log.warn("⚠️ 유효하지 않은 데이터 {}건 제외됨", apiItems.size() - pharmacies.size());
		}

		// 5. 저장
		pharmacyApiRepository.saveAll(pharmacies);
		int savedCount = pharmacies.size();

		log.info("✅ [{}] 지역 약국 데이터 수집 완료: {}건 저장", sgguCd, savedCount);
		return savedCount;
	}
}
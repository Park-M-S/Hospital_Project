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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyApiService {

	private final PharmacyApiCaller apiCaller;
	private final PharmacyApiRepository pharmacyApiRepository;
	private final PharmacyApiParser pharmacyApiParser;

	// 성남시 시군구 코드
	private static final String[] SEONGNAM_CODES = { "310401", "310402", "310403" };


	@Transactional
	public int fetchAndSaveSeongnamPharmacies() {
		log.info("🏥 성남시 전체 약국 데이터 수집 시작");

		// 1. 커스텀 삭제 메서드 사용
		pharmacyApiRepository.deleteAllPharmacies();
		log.info("🗑️ 기존 약국 데이터 전체 삭제 완료");
		
		pharmacyApiRepository.resetAutoIncrement();

		List<Pharmacy> allPharmacies = new ArrayList<>();
		Set<String> processedYkihos = new HashSet<>(); // 중복 방지를 위한 Set

		// 2. 각 구별로 데이터 수집
		for (String sgguCd : SEONGNAM_CODES) {
			log.info("🏥 [{}] 지역 약국 데이터 수집 중...", getDistrictName(sgguCd));

			OpenApiWrapper.Body body = apiCaller.callApiByDistrict(sgguCd);

			if (body == null || body.getItems() == null || body.getItems().isEmpty()) {
				log.warn("📭 [{}] 지역에 저장할 약국 정보 없음", getDistrictName(sgguCd));
				continue;
			}

			List<PharmacyApiItem> apiItems = body.getItems();
			log.info("📦 [{}] 지역 파싱된 약국 수: {}", getDistrictName(sgguCd), apiItems.size());

			// Entity 변환 (유효성 검사 포함)
			List<Pharmacy> pharmacies = pharmacyApiParser.parseToEntities(apiItems);
			if (pharmacies.size() != apiItems.size()) {
				log.warn("⚠️ [{}] 지역 유효하지 않은 데이터 {}건 제외됨", getDistrictName(sgguCd),
						apiItems.size() - pharmacies.size());
			}

			// 3. 중복 제거 처리
			int duplicateCount = 0;
			for (Pharmacy pharmacy : pharmacies) {
				String ykiho = pharmacy.getYkiho();
				if (ykiho != null && !processedYkihos.contains(ykiho)) {
					processedYkihos.add(ykiho);
					allPharmacies.add(pharmacy);
				} else {
					duplicateCount++;
					log.debug("🔄 중복 약국 제외: {}", pharmacy.getName());
				}
			}

			if (duplicateCount > 0) {
				log.info("🔄 [{}] 지역 중복 약국 {}건 제외됨", getDistrictName(sgguCd), duplicateCount);
			}

			log.info("✅ [{}] 지역 데이터 수집 완료: {}건 (중복 제외 후)", getDistrictName(sgguCd), pharmacies.size() - duplicateCount);
		}

		// 4. 한 번에 저장
		int totalSaved = 0;
		if (!allPharmacies.isEmpty()) {
			pharmacyApiRepository.saveAll(allPharmacies);
			totalSaved = allPharmacies.size();
			log.info("✅ 성남시 전체 약국 데이터 저장 완료: {}건 (중복 제거됨)", totalSaved);
		} else {
			log.warn("⚠️ 저장할 약국 데이터가 없음");
		}

		return totalSaved;
	}

	// 구 코드를 구 이름으로 변환 (로그 가독성을 위해)
	private String getDistrictName(String sgguCd) {
		switch (sgguCd) {
		case "310401":
			return "분당구";
		case "310402":
			return "수정구";
		case "310403":
			return "중원구";
		default:
			return sgguCd;
		}
	}

}
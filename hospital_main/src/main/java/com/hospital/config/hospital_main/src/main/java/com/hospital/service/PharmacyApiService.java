package com.hospital.service;

import com.hospital.caller.PharmacyApiCaller;
import com.hospital.config.RegionConfig;
import com.hospital.dto.OpenApiWrapper;
import com.hospital.dto.PharmacyApiItem;
import com.hospital.entity.Pharmacy;
import com.hospital.parser.PharmacyApiParser;
import com.hospital.repository.PharmacyApiRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class PharmacyApiService {

	private final PharmacyApiCaller pharmacyApiCaller;
	private final PharmacyApiRepository pharmacyApiRepository;
	private final PharmacyApiParser pharmacyApiParser;
	private final RegionConfig regionConfig;

	@Autowired
	public PharmacyApiService(PharmacyApiCaller pharmacyApiCaller, PharmacyApiRepository pharmacyApiRepository,
			PharmacyApiParser pharmacyApiParser, RegionConfig regionConfig) {
		this.pharmacyApiCaller = pharmacyApiCaller;
		this.pharmacyApiParser = pharmacyApiParser;
		this.pharmacyApiRepository = pharmacyApiRepository;
		this.regionConfig = regionConfig;
	}

	 public int fetchAndSaveSeongnamPharmacies() {
	        log.info("{} 전체 약국 데이터 수집 시작", regionConfig.getCityName());

	        try {
	            // 1. 기존 데이터 삭제
	            pharmacyApiRepository.deleteAllPharmacies();
	            log.info("기존 약국 데이터 전체 삭제 완료");
	            pharmacyApiRepository.resetAutoIncrement();

	            List<Pharmacy> allPharmacies = new ArrayList<>();
	            Set<String> processedYkihos = new HashSet<>();
	            List<String> sigunguCodes = regionConfig.getSigunguCodes();

	            // 2. 각 구별로 데이터 수집
	            for (String sgguCd : sigunguCodes) {
	                String districtName = regionConfig.getDistrictName(sgguCd);
	                try {
	                    log.info("[{}] 지역 약국 데이터 수집 중...", districtName);

	                    List<PharmacyApiItem> apiItems = pharmacyApiCaller.callApiByDistrict(sgguCd);

	                    if (apiItems == null || apiItems.isEmpty()) {
	                        log.warn("[{}] 지역에 저장할 약국 정보 없음", districtName);
	                        continue;
	                    }

	                    log.info("[{}] 지역 파싱된 약국 수: {}", districtName, apiItems.size());

	                    // Entity 변환
	                    List<Pharmacy> pharmacies = pharmacyApiParser.parseToEntities(apiItems);
	                    if (pharmacies.size() != apiItems.size()) {
	                        log.warn("[{}] 지역 유효하지 않은 데이터 {}건 제외됨", districtName, apiItems.size() - pharmacies.size());
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
							log.debug("중복 약국 제외: {}", pharmacy.getName());
						}
					}

					if (duplicateCount > 0) {
						log.info("[{}] 지역 중복 약국 {}건 제외됨", districtName, duplicateCount);
					}

					log.info("[{}] 지역 데이터 수집 완료: {}건 (중복 제외 후)", districtName, pharmacies.size() - duplicateCount);
					
				} catch (Exception e) {
					log.error("[{}] 지역 약국 데이터 수집 실패: {}", districtName, e.getMessage());
					// 한 지역 실패해도 다른 지역은 계속 처리
				}
			}

			// 4. 한 번에 저장
			int totalSaved = 0;
			if (!allPharmacies.isEmpty()) {
				pharmacyApiRepository.saveAll(allPharmacies);
				totalSaved = allPharmacies.size();
				log.info("{} 전체 약국 데이터 저장 완료: {}건 (중복 제거됨)", regionConfig.getCityName(), totalSaved);
			} else {
				log.warn("저장할 약국 데이터가 없음");
			}

			return totalSaved;
			
		} catch (Exception e) {
			log.error("약국 데이터 수집 실패", e);
			throw new RuntimeException("약국 데이터 수집 중 오류 발생: " + e.getMessage(), e);
		}
	}
}
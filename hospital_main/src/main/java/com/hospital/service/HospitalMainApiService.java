package com.hospital.service;

import com.hospital.config.RegionConfig; // 🔥 추가
import com.hospital.entity.HospitalMain;

import com.hospital.repository.HospitalMainApiRepository;

import lombok.extern.slf4j.Slf4j;


import com.hospital.caller.HospitalMainApiCaller;
import com.hospital.dto.api.HospitalMainApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hospital.parser.HospitalMainApiParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j

public class HospitalMainApiService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final HospitalMainApiCaller hospitalMainApiCaller;
	private final HospitalMainApiParser hospitalMainApiParser;
	private final RegionConfig regionConfig;

	@Autowired
	public HospitalMainApiService(HospitalMainApiRepository hospitalMainApiRepository,
			HospitalMainApiCaller hospitalMainApiCaller, HospitalMainApiParser hospitalMainApiParser,
			RegionConfig regionConfig) {
		this.hospitalMainApiCaller = hospitalMainApiCaller;
		this.hospitalMainApiParser = hospitalMainApiParser;
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.regionConfig = regionConfig;
		
	}

	public int fetchParseAndSaveHospitals() {
		log.info("병원 데이터 수집 시작 - 대상 지역: {}", regionConfig.getCityName());

		// ✅ 1. 기존 데이터 전체 삭제
		log.info("기존 병원 데이터 삭제 중...");
		long deletedCount = hospitalMainApiRepository.count();
		hospitalMainApiRepository.deleteAll();
		log.info("기존 병원 데이터 {}건 삭제 완료", deletedCount);

		int totalSavedOrUpdatedCount = 0;

		// regionConfig에서 시군구 코드 가져오기
		List<String> sigunguCodes = regionConfig.getSigunguCodes();

		for (String sgguCd : sigunguCodes) {
			try {
				log.info("지역 처리 시작: {} ({})", regionConfig.getDistrictName(sgguCd), sgguCd);
				int districtResult = processDistrictData(sgguCd);
				totalSavedOrUpdatedCount += districtResult;
			} catch (Exception e) {
				log.error("지역 {} 처리 중 오류 발생, 다음 지역으로 계속 진행: {}", regionConfig.getDistrictName(sgguCd), e.getMessage());
				// 한 지역 실패해도 다른 지역은 계속 처리
			}
		}

		log.info("병원 데이터 수집 완료: 총 {}건", totalSavedOrUpdatedCount);
		return totalSavedOrUpdatedCount;
	}

	private int processDistrictData(String sgguCd) {
		log.info("지역 {} 데이터 처리 시작", regionConfig.getDistrictName(sgguCd));

		int districtTotal = 0;
		int pageNo = 1;
		int numOfRows = 1000;
		boolean hasMorePages = true;

		while (hasMorePages) {
			try {
				// 1. API 호출
				HospitalMainApiResponse apiResponse = callApiForPage(sgguCd, pageNo, numOfRows);

				// 2. 파싱
				List<HospitalMain> hospitals = hospitalMainApiParser.parseHospitals(apiResponse);

				if (hospitals.isEmpty()) {
					log.info("지역 {} 페이지 {}: 더 이상 데이터 없음", regionConfig.getDistrictName(sgguCd), pageNo);
					hasMorePages = false;
					continue;
				}

				// 3. 저장
				hospitalMainApiRepository.saveAll(hospitals);
				districtTotal += hospitals.size();

				log.info("지역 {} 페이지 {}: {}건 저장 완료", regionConfig.getDistrictName(sgguCd), pageNo, hospitals.size());

				// 4. 페이징 처리
				hasMorePages = determineNextPage(apiResponse, hospitals.size(), numOfRows);
				pageNo++;

				// 5. API 호출 제한
				Thread.sleep(5000);

			} catch (Exception e) {
				log.error("지역 {} 페이지 {} 처리 실패: {}", regionConfig.getDistrictName(sgguCd), pageNo, e.getMessage());
				throw new RuntimeException("지역 " + regionConfig.getDistrictName(sgguCd) + " 처리 중 오류 발생", e);
			}
		}

		log.info("지역 {} 처리 완료: {}건", regionConfig.getDistrictName(sgguCd), districtTotal);
		return districtTotal;
	}

	private HospitalMainApiResponse callApiForPage(String sgguCd, int pageNo, int numOfRows) {
		String encodedSgguCd;
		try {
			encodedSgguCd = URLEncoder.encode(sgguCd, StandardCharsets.UTF_8.toString());
		} catch (Exception e) {
			throw new RuntimeException("시군구 코드 인코딩 실패: " + e.getMessage(), e);
		}

		String apiPath = "hospInfoServicev2/getHospBasisList";
		String queryParams = String.format("pageNo=%d&numOfRows=%d&sgguCd=%s", pageNo, numOfRows, encodedSgguCd);

		return hospitalMainApiCaller.callApi(apiPath, queryParams);
	}

	private boolean determineNextPage(HospitalMainApiResponse response, int currentBatchSize, int numOfRows) {
		int totalCount = Optional.ofNullable(response).map(HospitalMainApiResponse::getResponse)
				.map(HospitalMainApiResponse.Response::getBody).map(HospitalMainApiResponse.Body::getTotalCount)
				.orElse(0);

		return currentBatchSize == numOfRows && totalCount > 0;
	}
}

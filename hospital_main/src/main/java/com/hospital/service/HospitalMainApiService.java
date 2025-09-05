package com.hospital.service;

import com.hospital.config.RegionConfig;

import com.hospital.repository.HospitalMainApiRepository;

import lombok.extern.slf4j.Slf4j;

import com.hospital.async.HospitalMainAsyncRunner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j

public class HospitalMainApiService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final HospitalMainAsyncRunner hospitalMainAsyncRunner;
	private final RegionConfig regionConfig;

	@Autowired
	public HospitalMainApiService(HospitalMainApiRepository hospitalMainApiRepository,
			HospitalMainAsyncRunner hospitalMainAsyncRunner, RegionConfig regionConfig) {
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.hospitalMainAsyncRunner = hospitalMainAsyncRunner;
		this.regionConfig = regionConfig;

	}

	public int fetchParseAndSaveHospitals() {
		log.info("병원 데이터 수집 시작 - 대상 지역: {}", regionConfig.getCityName());

		// 1. 기존 데이터 전체 삭제
		log.info("기존 병원 데이터 삭제 중...");
		long deletedCount = hospitalMainApiRepository.count();
		hospitalMainApiRepository.deleteAll();
		log.info("기존 병원 데이터 {}건 삭제 완료", deletedCount);

		// regionConfig에서 시군구 코드 가져오기
		List<String> sigunguCodes = regionConfig.getSigunguCodes();
		hospitalMainAsyncRunner.resetCounter();
		hospitalMainAsyncRunner.setTotalCount(sigunguCodes.size());

		for (String sgguCd : sigunguCodes) {
			hospitalMainAsyncRunner.runAsync(sgguCd);
		}
		log.info("{}개 지역 병렬 처리 시작", sigunguCodes.size());
		return sigunguCodes.size(); // 총 지역 수만 반환

	}

	public int getCompletedCount() {
		return hospitalMainAsyncRunner.getCompletedCount();
	}

	public int getFailedCount() {
		return hospitalMainAsyncRunner.getFailedCount();
	}
}

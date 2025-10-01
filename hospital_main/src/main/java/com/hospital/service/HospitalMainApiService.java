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

	public int updateHospitalMain() {
		log.info("병원 데이터 수집 시작 - 대상 지역: {}", regionConfig.getCityName());
		
		hospitalMainApiRepository.deleteAllInBatch();

		// regionConfig에서 시군구 코드 가져오기
		List<String> sidoCodes = regionConfig.getNationwideSidoCodes();
		hospitalMainAsyncRunner.resetCounter();
		hospitalMainAsyncRunner.setTotalCount(sidoCodes.size());

		for (String sidoCd : sidoCodes) {
			hospitalMainAsyncRunner.runAsync(sidoCd);
		}
		log.info("{}개 지역 병렬 처리 시작", sidoCodes.size());
		return sidoCodes.size(); // 총 지역 수만 반환

	}

	public int getCompletedCount() {
		return hospitalMainAsyncRunner.getCompletedCount();
	}

	public int getFailedCount() {
		return hospitalMainAsyncRunner.getFailedCount();
	}
}
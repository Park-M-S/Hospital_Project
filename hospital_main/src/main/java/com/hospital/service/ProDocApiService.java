package com.hospital.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.async.ProDocAsyncRunner;
import com.hospital.config.RegionConfig;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.repository.ProDocApiRepository;

import lombok.extern.slf4j.Slf4j;




//ProDocServiceImpl 전문의(ProDoc) 정보 수집 및 저장 기능 구현체
@Service
@Slf4j
public class ProDocApiService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final ProDocAsyncRunner proDocAsyncRunner;
	private final ProDocApiRepository proDocApiRepository;
	private final RegionConfig regionConfig;

	@Autowired
	public ProDocApiService(HospitalMainApiRepository hospitalMainApiRepository, ProDocAsyncRunner proDocAsyncRunner,
			ProDocApiRepository proDocApiRepository, RegionConfig regionConfig) {
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.proDocAsyncRunner = proDocAsyncRunner;
		this.proDocApiRepository = proDocApiRepository;
		this.regionConfig = regionConfig;
	}

	public int updateProDoc() {
		log.info("병원 데이터 수집 시작 - 대상 지역: {}", regionConfig.getCityName());
		
		log.info("기존 병원 데이터 전체 삭제 시작...");
        proDocApiRepository.deleteAll();
        log.info("기존 병원 데이터 전체 삭제 완료");

		// regionConfig에서 시군구 코드 가져오기
		List<String> sidoCodes = regionConfig.getNationwideSidoCodes();
		proDocAsyncRunner.resetCounter();
		proDocAsyncRunner.setTotalCount(sidoCodes.size());

		for (String sidoCd : sidoCodes) {
			proDocAsyncRunner.runAsync(sidoCd);
		}
		log.info("{}개 지역 병렬 처리 시작", sidoCodes.size());
		return sidoCodes.size(); // 총 지역 수만 반환

	}

	public int getCompletedCount() {
		return proDocAsyncRunner.getCompletedCount();
	}

	public int getFailedCount() {
		return proDocAsyncRunner.getFailedCount();
	}
}
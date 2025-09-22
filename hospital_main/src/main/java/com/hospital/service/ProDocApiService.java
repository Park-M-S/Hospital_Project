package com.hospital.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hospital.async.ProDocAsyncRunner;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.repository.ProDocApiRepository;

import lombok.extern.slf4j.Slf4j;




//ProDocServiceImpl 전문의(ProDoc) 정보 수집 및 저장 기능 구현체
@Service
@Slf4j
public class ProDocApiService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final ProDocAsyncRunner proDocAsyncRunner;
	private final ProDocApiRepository proDocRepository;

	@Autowired
	public ProDocApiService(HospitalMainApiRepository hospitalMainApiRepository, ProDocAsyncRunner proDocAsyncRunner,
			ProDocApiRepository proDocRepository) {
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.proDocAsyncRunner = proDocAsyncRunner;
		this.proDocRepository = proDocRepository;
	}

	public int updateProDocs() {
		try {
			

			// 병원 코드 리스트 불러오기
			List<String> hospitalCodes = hospitalMainApiRepository.findAllHospitalCodes();
			if (hospitalCodes.isEmpty()) {
				throw new IllegalStateException("병원 기본정보가 없어 전문의 정보를 수집할 수 없습니다");
			}

			// 비동기 상태 초기화
			proDocAsyncRunner.resetCounter();
			
			proDocAsyncRunner.runBatchAsync(hospitalCodes);
			
			 return hospitalCodes.size();
			
		} catch (Exception e) {
			log.error("전문의 정보 수집 실패", e);
			throw new RuntimeException("전문의 정보 수집 중 오류 발생: " + e.getMessage(), e);
		}
	}

	public int getCompletedCount() {
		return proDocAsyncRunner.getCompletedCount();
	}

	public int getFailedCount() {
		return proDocAsyncRunner.getFailedCount();
	}
}
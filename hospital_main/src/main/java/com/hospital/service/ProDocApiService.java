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
	private final ProDocAsyncRunner proDocasyncRunner;
	private final ProDocApiRepository proDocRepository;

	@Autowired
	public ProDocApiService(HospitalMainApiRepository hospitalMainApiRepository, ProDocAsyncRunner proDocasyncRunner,
			ProDocApiRepository proDocRepository) {
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.proDocasyncRunner = proDocasyncRunner;
		this.proDocRepository = proDocRepository;
	}

	public int fetchParseAndSaveProDocs() {
		try {
			// 기존 데이터 전체 삭제
			proDocRepository.deleteAllProDocs();
			proDocRepository.resetAutoIncrement();

			// 병원 코드 리스트 불러오기
			List<String> hospitalCodes = hospitalMainApiRepository.findAllHospitalCodes();
			if (hospitalCodes.isEmpty()) {
				throw new IllegalStateException("병원 기본정보가 없어 전문의 정보를 수집할 수 없습니다");
			}

			// 비동기 상태 초기화
			proDocasyncRunner.resetCounter();
			proDocasyncRunner.setTotalCount(hospitalCodes.size());

			// 병원 코드별 API 호출
			for (String hospitalCode : hospitalCodes) {
				proDocasyncRunner.runAsync(hospitalCode);
			}

			return hospitalCodes.size();
			
		} catch (Exception e) {
			log.error("전문의 정보 수집 실패", e);
			throw new RuntimeException("전문의 정보 수집 중 오류 발생: " + e.getMessage(), e);
		}
	}

	public int getCompletedCount() {
		return proDocasyncRunner.getCompletedCount();
	}

	public int getFailedCount() {
		return proDocasyncRunner.getFailedCount();
	}
}
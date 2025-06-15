package com.hospital.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.hospital.async.ProDocAsyncRunner;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.repository.ProDocApiRepository;



/**
 * 🧠 ProDocServiceImpl 전문의(ProDoc) 정보 수집 및 저장 기능 구현체
 */
@Service
public class ProDocApiService {

	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final ProDocAsyncRunner proDocasyncRunner; // 전문의 API 비동기 실행기
	private final ProDocApiRepository proDocRepository; // 전문의 정보 저장소 (JPA)

	@Autowired
	public ProDocApiService(HospitalMainApiRepository hospitalMainApiRepository, ProDocAsyncRunner proDocasyncRunner,
			ProDocApiRepository proDocRepository) {
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.proDocasyncRunner = proDocasyncRunner;
		this.proDocRepository = proDocRepository;

	}

	public int fetchParseAndSaveProDocs() {
		// 기존 데이터 전체 삭제
		proDocRepository.deleteAllProDocs();

		proDocRepository.resetAutoIncrement();

		// 병원 코드 리스트 불러오기
		List<String> hospitalCodes = hospitalMainApiRepository.findAllHospitalCodes();

		// 비동기 상태 초기화
		proDocasyncRunner.resetCounter();
		proDocasyncRunner.setTotalCount(hospitalCodes.size());

		// 병원 코드별 API 호출
		for (String hospitalCode : hospitalCodes) {
			proDocasyncRunner.runAsync(hospitalCode); // 🔁 비동기 실행
		}

		return hospitalCodes.size(); // 전체 병원 수 반환
	}

	/**
	 * ✅ 완료된 병원 처리 수 조회
	 */

	public int getCompletedCount() {
		return proDocasyncRunner.getCompletedCount();
	}

	/**
	 * ✅ 실패한 병원 처리 수 조회
	 */

	public int getFailedCount() {
		return proDocasyncRunner.getFailedCount();
	}
}

package com.hospital.service;

import com.hospital.async.ProDocAsyncRunner;
import com.hospital.repository.ProDocApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 🧠 ProDocServiceImpl 전문의(ProDoc) 정보 수집 및 저장 기능 구현체
 */
@Service
public class ProDocApiServiceImpl implements ProDocApiService {

	private final HospitalMainApiService hospitalMainService; // 병원 목록 가져오는 서비스
	private final ProDocAsyncRunner asyncRunner; // 전문의 API 비동기 실행기
	private final ProDocApiRepository proDocRepository; // 전문의 정보 저장소 (JPA)

	@Autowired
	public ProDocApiServiceImpl(HospitalMainApiService hospitalMainService, ProDocAsyncRunner asyncRunner,
			ProDocApiRepository proDocRepository) {
		this.hospitalMainService = hospitalMainService;
		this.asyncRunner = asyncRunner;
		this.proDocRepository = proDocRepository;
	}

	/**
	 * ✅ 병원 전체를 대상으로 API 호출 후 전문의 데이터 비동기 저장 실행 1. 기존 전문의 데이터 모두 삭제 2. 전체 병원코드 가져오기
	 * 3. 비동기 상태 초기화 및 총 작업 수 설정 4. 각 병원코드마다 runAsync() 호출
	 *
	 * @return 처리 대상 병원 수
	 */
	@Override
	@Transactional
	public int fetchParseAndSaveProDocs() {
		// 기존 데이터 전체 삭제
		proDocRepository.deleteAllProDocs();

		proDocRepository.resetAutoIncrement();

		// 병원 코드 리스트 불러오기
		List<String> hospitalCodes = hospitalMainService.getAllHospitalCodes();

		// 비동기 상태 초기화
		asyncRunner.resetCounter();
		asyncRunner.setTotalCount(hospitalCodes.size());

		// 병원 코드별 API 호출
		for (String hospitalCode : hospitalCodes) {
			asyncRunner.runAsync(hospitalCode); // 🔁 비동기 실행
		}

		return hospitalCodes.size(); // 전체 병원 수 반환
	}

	/**
	 * ✅ 완료된 병원 처리 수 조회
	 */
	@Override
	public int getCompletedCount() {
		return asyncRunner.getCompletedCount();
	}

	/**
	 * ✅ 실패한 병원 처리 수 조회
	 */
	@Override
	public int getFailedCount() {
		return asyncRunner.getFailedCount();
	}
}

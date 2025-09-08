package com.hospital.async;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.HospitalDetailApiCaller;
import com.hospital.dto.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.HospitalMain;
import com.hospital.parser.HospitalDetailApiParser;
import com.hospital.repository.HospitalDetailApiRepository;
import com.hospital.repository.HospitalMainApiRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HospitalDetailAsyncRunner {
	private final RateLimiter rateLimiter = RateLimiter.create(8.0); // 초당 8건 제한

	// 의존성 주입: API 호출, 파싱, 저장을 담당하는 객체들
	private final HospitalDetailApiCaller apiCaller;
	private final HospitalDetailApiParser parser;
	private final HospitalDetailApiRepository hospitalDetailrepository;
	private final HospitalMainApiRepository hospitalMainApiRepository; 


	// 처리 상태 추적용 카운터
	private final AtomicInteger completedCount = new AtomicInteger(0); // 성공
	private final AtomicInteger failedCount = new AtomicInteger(0); // 실패
	private int totalCount = 0; // 전체 병원 수

	@Autowired
	public HospitalDetailAsyncRunner(HospitalDetailApiCaller apiCaller, HospitalDetailApiParser parser,
			HospitalDetailApiRepository hospitalDetailrepository,
			HospitalMainApiRepository hospitalMainRepository) {
		this.apiCaller = apiCaller;
		this.parser = parser;
		this.hospitalDetailrepository = hospitalDetailrepository;
		this.hospitalMainApiRepository = hospitalMainRepository;
	}

	// 진행 상태 초기화
	public void resetCounter() {
		completedCount.set(0);
		failedCount.set(0);
	}

	// 총 작업 수 설정 및 카운터 초기화
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		completedCount.set(0);
		failedCount.set(0);
	}

	// 현재까지 완료된 작업 수
	public int getCompletedCount() {
		return completedCount.get();
	}

	// 현재까지 실패한 작업 수
	public int getFailedCount() {
		return failedCount.get();
	}

	// 병원코드 단위 비동기 처리
	@Async("apiExecutor") // 별도의 실행자 풀 사용
	public void runAsync(String hospitalCode) {
		rateLimiter.acquire(); // 이 한 줄로 초당 호출 제한 적용됨

		try {
			// 1. 병원코드를 쿼리 파라미터로 설정
			String queryParams = String.format("ykiho=%s", hospitalCode);
			log.info("API 파라미터: {}", hospitalCode);

			// 2. 공공 API 호출 → JSON 파싱 → DTO 매핑
			HospitalDetailApiResponse response = apiCaller.callApi(queryParams);

			// 3. DTO → Entity 리스트 변환
			List<HospitalDetail> parsed = parser.parse(response, hospitalCode);

			// 4. 변환된 데이터가 있을 경우에만 저장
			if (!parsed.isEmpty()) {
				   HospitalMain hospitalMain = hospitalMainApiRepository.findById(hospitalCode)

	                        .orElseThrow(() -> new IllegalArgumentException("병원 메인 정보 없음: " + hospitalCode));


					for (HospitalDetail detail : parsed) {

						detail.setHospitalCode(hospitalCode);

						detail.setHospital(hospitalMain); //연관관계 설정

						log.info("실제 저장값: {}, 연결된 병원명: {}", detail.getHospitalCode(),
								hospitalMain.getHospitalName());

					}
				
				hospitalDetailrepository.saveAll(parsed);
			}
	

			// 5. 완료 카운터 증가 + 로그 출력
			int done = completedCount.incrementAndGet();
			log.info("처리됨: {} / {} ({}%)", done, totalCount, (done * 100) / totalCount);

		}catch(

	Exception e)
	{
		// 예외 발생 시 실패 카운터 증가 + 로그 출력
		failedCount.incrementAndGet();
		log.error("병원코드 {} 처리 중 오류: {}", hospitalCode, e.getMessage());
	}
}}

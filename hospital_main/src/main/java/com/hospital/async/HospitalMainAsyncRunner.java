package com.hospital.async;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.beans.factory.annotation.Autowired;
import com.hospital.caller.HospitalMainApiCaller;
import com.hospital.parser.HospitalMainApiParser;
import com.hospital.repository.HospitalMainApiRepository;
import com.hospital.dto.HospitalMainApiResponse;
import com.hospital.entity.HospitalMain;
import com.hospital.config.RegionConfig;

@Service
@Slf4j
public class HospitalMainAsyncRunner {
	private final RateLimiter rateLimiter = RateLimiter.create(2.0);
	private final AtomicInteger completedCount = new AtomicInteger(0);
	private final AtomicInteger failedCount = new AtomicInteger(0);
	private int totalCount = 0;

	private final HospitalMainApiCaller apiCaller;
	private final HospitalMainApiParser parser;
	private final HospitalMainApiRepository hospitalMainApiRepository;
	private final RegionConfig regionConfig;

	@Autowired
	public HospitalMainAsyncRunner(HospitalMainApiCaller apiCaller, HospitalMainApiParser parser,
			HospitalMainApiRepository hospitalMainApiRepository, RegionConfig regionConfig) {
		this.apiCaller = apiCaller;
		this.parser = parser;
		this.hospitalMainApiRepository = hospitalMainApiRepository;
		this.regionConfig = regionConfig;
	}

	@Async("apiExecutor")
	public void runAsync(String sgguCd) {
		rateLimiter.acquire();
		try {
			log.info("지역코드 {} 처리 시작", regionConfig.getDistrictName(sgguCd));

			// 유효성 검사
			if (sgguCd == null || sgguCd.trim().isEmpty()) {
				throw new IllegalArgumentException("지역코드가 비어있습니다");
			}

			int totalSaved = 0;
			int pageNo = 1;
			int numOfRows = 1000;
			boolean hasMorePages = true;

			while (hasMorePages) {

				String queryParams = String.format("sgguCd=%s&pageNo=%s&numOfRows=%s", sgguCd, pageNo, numOfRows);
				HospitalMainApiResponse response = apiCaller.callApi(queryParams);

				List<HospitalMain> hospitals = parser.parseHospitals(response);

				if (hospitals.isEmpty()) {
					log.info("지역{} 페이지{} : 더이상 데이터 없음", regionConfig.getDistrictName(sgguCd), pageNo);

					break;
				}
				hospitalMainApiRepository.saveAll(hospitals);
				totalSaved += hospitals.size();

				log.info("지역 {} 페이지 {}: {}건 저장", regionConfig.getDistrictName(sgguCd), pageNo, hospitals.size());
				hasMorePages = hospitals.size() >= numOfRows;
				pageNo++;

				// 페이지 간 대기
				Thread.sleep(1000);
			}
			completedCount.incrementAndGet();
			log.info("지역 {} 처리 완료: 총 {}건 저장", regionConfig.getDistrictName(sgguCd), totalSaved);

		} catch (Exception e) {
			failedCount.incrementAndGet();
			log.error("지역 코드 {} 처리 실패: {}", regionConfig.getDistrictName(sgguCd), e.getMessage());
		}

	}

	public int getCompletedCount() {
		return completedCount.get();
	}

	public int getFailedCount() {
		return failedCount.get();
	}

	public void resetCounter() {
		completedCount.set(0);
		failedCount.set(0);
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
		completedCount.set(0);
		failedCount.set(0);
	}

}

package com.hospital.async;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.client.HospitalDetailApiCaller;
import com.hospital.dto.api.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import com.hospital.parser.HospitalDetailApiParser;
import com.hospital.repository.HospitalDetailApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service // Spring 서비스 컴포넌트로 등록 (비즈니스 로직 실행 담당)
public class HospitalDetailAsyncRunner {
    private final RateLimiter rateLimiter = RateLimiter.create(3.0); // 초당 3건 제한

    // 의존성 주입: API 호출, 파싱, 저장을 담당하는 객체들
    private final HospitalDetailApiCaller apiCaller;
    private final HospitalDetailApiParser parser;
    private final HospitalDetailApiRepository repository;

    // 처리 상태 추적용 카운터
    private final AtomicInteger completedCount = new AtomicInteger(0); // 성공
    private final AtomicInteger failedCount = new AtomicInteger(0);    // 실패
    private int totalCount = 0; // 전체 병원 수

    @Autowired
    public HospitalDetailAsyncRunner(HospitalDetailApiCaller apiCaller,
                                   HospitalDetailApiParser parser,
                                   HospitalDetailApiRepository repository) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.repository = repository;
    }

    // ✅ 진행 상태 초기화
    public void resetCounter() {
        completedCount.set(0);
        failedCount.set(0);
    }

    // ✅ 총 작업 수 설정 및 카운터 초기화
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        completedCount.set(0);
        failedCount.set(0);
    }

    // ✅ 현재까지 완료된 작업 수
    public int getCompletedCount() {
        return completedCount.get();
    }

    // ✅ 현재까지 실패한 작업 수
    public int getFailedCount() {
        return failedCount.get();
    }

    // ✅ 병원코드 단위 비동기 처리
    @Async("hospitalDetailExecutor") // 별도의 실행자 풀 사용
    public void runAsync(String hospitalCode) {
        rateLimiter.acquire(); // 🔒 이 한 줄로 초당 호출 제한 적용됨

        try {
            // 1. 병원코드를 쿼리 파라미터로 설정
            String queryParams = String.format("ykiho=%s", hospitalCode);
            log.info("🔍 API 파라미터: {}", hospitalCode); 

            // 2. 공공 API 호출 → JSON 파싱 → DTO 매핑
            HospitalDetailApiResponse response = apiCaller.callApi("getDtlInfo2.7", queryParams);

            // 3. DTO → Entity 리스트 변환
            List<HospitalDetail> parsed = parser.parse(response, hospitalCode);

            // 4. 변환된 데이터가 있을 경우에만 저장
            if (!parsed.isEmpty()) {
            	 for (HospitalDetail entity : parsed) {
                     log.info("🔍 실제 저장값: {}", entity.getHospitalCode()); // ← 여기 추가
                 }
                repository.saveAll(parsed);
            }

            // 5. 완료 카운터 증가 + 로그 출력
            int done = completedCount.incrementAndGet();
            log.info("✅ 처리됨: {} / {} ({}%)", done, totalCount, (done * 100) / totalCount);

        } catch (Exception e) {
            // 예외 발생 시 실패 카운터 증가 + 로그 출력
            failedCount.incrementAndGet();
            log.error("❌ 병원코드 {} 처리 중 오류: {}", hospitalCode, e.getMessage());
        }
    }
}
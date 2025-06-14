package com.hospital.async;

import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.MedicalSubjectApiCaller;
import com.hospital.dto.api.MedicalSubjectApiResponse;
import com.hospital.entity.MedicalSubject;
import com.hospital.parser.MedicalSubjectApiParser;
import com.hospital.repository.MedicalSubjectApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service // 서비스 컴포넌트로 등록 (비즈니스 로직)
public class MedicalSubjectApiAsyncRunner {
	private final RateLimiter rateLimiter = RateLimiter.create(8.0);


    // 의존성 주입: API 호출, 파싱, DB 저장을 담당하는 클래스들
    private final MedicalSubjectApiCaller apiCaller;
    private final MedicalSubjectApiParser parser;
    private final MedicalSubjectApiRepository repository;

    // 병원 처리 결과를 추적하기 위한 카운터 (쓰레드 안전)
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private int totalCount = 0; // 전체 병원 수

    @Autowired
    public MedicalSubjectApiAsyncRunner(
            MedicalSubjectApiCaller apiCaller,
            MedicalSubjectApiParser parser,
            MedicalSubjectApiRepository repository) {
        this.apiCaller = apiCaller;
        this.parser = parser;
        this.repository = repository;
    }

    // 병원 총 수를 설정하고 카운터 초기화
    public void setTotalCount(int count) {
        this.totalCount = count;
        resetCounter();
    }

    // 완료/실패 카운터 초기화
    public void resetCounter() {
        completedCount.set(0);
        failedCount.set(0);
    }

    public int getCompletedCount() {
        return completedCount.get();
    }

    public int getFailedCount() {
        return failedCount.get();
    }

    @Async("apiExecutor") // ✅ 병렬 실행을 위한 스레드 풀 사용
    public void runAsync(String hospitalCode) {
    	rateLimiter.acquire();;
        try {
            // ✅ API 파라미터 구성
            String queryParams = "ykiho=" + hospitalCode;

            // ✅ API 호출 및 JSON → DTO 매핑
            MedicalSubjectApiResponse response = apiCaller.callApi("getDgsbjtInfo2.7", queryParams);

            // ✅ 응답 파싱 → 진료과목 리스트 변환
            List<MedicalSubject> subjects = parser.parse(response, hospitalCode);

            // ✅ 병원코드 기준으로 기존 데이터 삭제
            repository.deleteByHospitalCode(hospitalCode);

            // ✅ 파싱한 진료과목 데이터 저장
            repository.saveAll(subjects);

            // ✅ 완료 카운터 증가 및 로그 출력
            int done = completedCount.incrementAndGet();
            log.info("✅ 진료과목 저장 완료: {} / {} ({}%)", done, totalCount, (done * 100) / totalCount);

        } catch (Exception e) {
            // ✅ 실패 카운터 증가 및 오류 로그
            //int fail = failedCount.incrementAndGet();
            log.error("❌ 병원코드 {} 처리 실패: {}", hospitalCode, e.getMessage());
        }
    }
}

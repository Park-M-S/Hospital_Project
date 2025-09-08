package com.hospital.async;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


@Slf4j
public class HospitalMainAsyncRunnerTest {
	
	@Test
	public void testRunner() {
		log.info("테스트 시작!");
		
		HospitalMainAsyncRunner runner = new HospitalMainAsyncRunner();
		
		int initialCompleted = runner.getCompletedCount();
		int initialFailed = runner.getFailedCount();
		log.info("초기값 - 완료: {}, 실패: {}", initialCompleted, initialFailed);
		
		runner.runAsync("test_code");
		
		log.info("테스트 완료!");
	}
}

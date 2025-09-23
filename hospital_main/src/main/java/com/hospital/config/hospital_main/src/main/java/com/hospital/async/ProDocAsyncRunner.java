package com.hospital.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.google.common.util.concurrent.RateLimiter;
import com.hospital.caller.ProDocApiCaller;
import com.hospital.dto.ProDocApiResponse;
import com.hospital.entity.ProDoc;
import com.hospital.parser.ProDocApiParser;
import com.hospital.repository.ProDocApiRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProDocAsyncRunner {
	private final RateLimiter rateLimiter = RateLimiter.create(8.0);
	private final Executor executor;
	private final ProDocApiCaller apiCaller;
	private final ProDocApiParser parser;
	private final ProDocApiRepository repository;
	private final AtomicInteger completedCount = new AtomicInteger(0);
	private final AtomicInteger failedCount = new AtomicInteger(0);
	private final AtomicInteger insertedCount = new AtomicInteger(0);
	private static final int CHUNK_SIZE = 100;
	private static final int BATCH_SIZE = 1000;

	@Autowired
	public ProDocAsyncRunner(ProDocApiCaller apiCaller, ProDocApiParser parser, ProDocApiRepository repository,
			@Qualifier("apiExecutor") Executor executor) {
		this.apiCaller = apiCaller;
		this.parser = parser;
		this.repository = repository;
		this.executor = executor;
	}

	@Async("apiExecutor")
	public void runBatchAsync(List<String> hospitalCodes) {
		log.info("전문의 정보 배치(전체 삭제 후 삽입) 시작: 총 {}건", hospitalCodes.size());
		try {
			// 1. 청크 분할
			List<List<String>> chunks = partitionList(hospitalCodes, CHUNK_SIZE);

			// 2. 청크별 비동기 처리
			List<CompletableFuture<Void>> futures = new ArrayList<>();
			for (List<String> chunk : chunks) {
				futures.add(CompletableFuture.runAsync(() -> processAndSaveChunk(chunk), executor));
			}
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			log.info("전문의 정보 배치 완료: 완료 {}, 실패 {}, 신규 {}", completedCount.get(), failedCount.get(), insertedCount.get());
		} catch (Exception e) {
			failedCount.addAndGet(hospitalCodes.size());
			log.error("전체 배치 실패", e);
		}
	}

	private void processAndSaveChunk(List<String> chunk) {

		String threadName = Thread.currentThread().getName();

		log.debug("[{}] 청크 처리 시작: {}건", threadName, chunk.size());

		List<ProDoc> batch = new ArrayList<>();

		for (String hospitalCode : chunk) {
			try {
				rateLimiter.acquire();
				String queryParams = "ykiho=" + hospitalCode;
				ProDocApiResponse response = apiCaller.callApi(queryParams);
				List<ProDoc> parsedList = parser.parse(response, hospitalCode);
				batch.addAll(parsedList);
				completedCount.incrementAndGet();
				// 배치 단위로 저장
				if (batch.size() >= BATCH_SIZE) {
					repository.saveAll(batch);
					insertedCount.addAndGet(batch.size());
					batch.clear();
				}
			} catch (Exception e) {
				failedCount.incrementAndGet();
				log.error("[{}] API 호출 실패: {}", threadName, hospitalCode, e);
			}
		}
		// 남은 배치 저장
		if (!batch.isEmpty()) {
			repository.saveAll(batch);
			insertedCount.addAndGet(batch.size());
		}
		log.debug("[{}] 청크 처리 완료: {}건 저장", threadName, batch.size());
	}

	private List<List<String>> partitionList(List<String> list, int size) {
		List<List<String>> chunks = new ArrayList<>();
		for (int i = 0; i < list.size(); i += size) {
			chunks.add(list.subList(i, Math.min(i + size, list.size())));
		}
		return chunks;
	}

	// 상태 관리
	public void resetCounter() {
		completedCount.set(0);
		failedCount.set(0);
		insertedCount.set(0);
	}

	public int getCompletedCount() {
		return completedCount.get();
	}

	public int getFailedCount() {
		return failedCount.get();
	}

	public int getInsertedCount() {
		return insertedCount.get();
	}
}
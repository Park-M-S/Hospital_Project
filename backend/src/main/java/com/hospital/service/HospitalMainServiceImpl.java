
package com.hospital.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hospital.entity.Hospital;
import com.hospital.repository.HospitalMainRepository;
import com.hospital.client.HospitalMainInfoApiCaller;
import com.hospital.dto.api.HospitalMainApiResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.hospital.parser.HospitalMainInfoApiParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HospitalMainServiceImpl implements HospitalMainService {

	private final HospitalMainRepository hospitalMainRepository;
	private final HospitalMainInfoApiCaller hospitalMainInfoApiCaller;
	private final HospitalMainInfoApiParser hospitalMainInfoApiParser;

	@Autowired
	public HospitalMainServiceImpl(HospitalMainRepository hospitalMainRepository,
			HospitalMainInfoApiCaller hospitalMainInfoApiCaller, HospitalMainInfoApiParser hospitalMainInfoApiParser) {
		this.hospitalMainRepository = hospitalMainRepository;
		this.hospitalMainInfoApiCaller = hospitalMainInfoApiCaller;
		this.hospitalMainInfoApiParser = hospitalMainInfoApiParser;
	}

	private final List<String> sigunguCodes = Arrays.asList("310401", "310402", "310403");

	public int fetchParseAndSaveHospitals() {
		System.out.println("Starting to fetch, parse, and save hospitals to DB...");
		int totalSavedOrUpdatedCount = 0;

		for (String sgguCd : sigunguCodes) {
			System.out.println("Processing sigunguCode: " + sgguCd);
			int pageNo = 1;
			int numOfRows = 1000;
			boolean hasMorePages = true;
			int currentPageFetchedCount = 0;

			while (hasMorePages) {
				String encodedSgguCd;
				try {
					encodedSgguCd = URLEncoder.encode(sgguCd, StandardCharsets.UTF_8.toString());
				} catch (Exception e) {
					System.err.println("시군구 코드 인코딩 실패: " + e.getMessage());
					break;
				}

				String apiPath = "hospInfoServicev2/getHospBasisList";
				String queryParams = String.format("pageNo=%d&numOfRows=%d&sgguCd=%s", pageNo, numOfRows,
						encodedSgguCd);

				try {
					// ★★★ API Caller의 반환 타입 변경에 맞춰 수정 ★★★
					HospitalMainApiResponse apiResponseDto = hospitalMainInfoApiCaller.callApi(apiPath, queryParams);

					// ★★★ Parser에게 DTO 객체를 넘겨줌 ★★★
					List<Hospital> currentBatch = hospitalMainInfoApiParser.parseHospitals(apiResponseDto);

					// ★★★ totalCount도 DTO 객체를 통해 접근 ★★★
					// Optional을 사용하여 null-safe하게 접근
					int totalCount = Optional.ofNullable(apiResponseDto).map(HospitalMainApiResponse::getResponse)
							.map(HospitalMainApiResponse.Response::getBody)
							.map(HospitalMainApiResponse.Body::getTotalCount).orElse(0); // totalCount가 없을 경우 0으로 기본값 설정

					if (!currentBatch.isEmpty()) {

						hospitalMainRepository.saveAll(currentBatch);

						totalSavedOrUpdatedCount += currentBatch.size(); // 총 개수 누적
						currentPageFetchedCount += currentBatch.size();
						System.out.println(
								"Sigungu " + sgguCd + ", Page " + pageNo + ": Fetched and saved " + currentBatch.size()
										+ " hospitals. Total fetched for sigungu: " + currentPageFetchedCount);
						// 페이징 로직 개선: 현재 배치 사이즈가 numOfRows와 같고, 아직 총 개수에 도달하지 않았으면 다음 페이지로
						if (currentPageFetchedCount < totalCount && currentBatch.size() == numOfRows) {
							pageNo++;
						} else {
							hasMorePages = false;
						}
						Thread.sleep(5000); // 과도한 API 호출 방지를 위해 잠시 대기

					} else {
						hasMorePages = false;
						System.out.println("Sigungu " + sgguCd + ", Page " + pageNo + ": No more data found.");
					}

				} catch (RuntimeException e) {
					System.err.println("API 호출 또는 응답 처리 중 오류 발생 for sigunguCode " + sgguCd + ", page " + pageNo + ": "
							+ e.getMessage());
					e.printStackTrace();
					hasMorePages = false;
				} catch (Exception e) {
					System.err.println(
							"알 수 없는 오류 발생 for sigunguCode " + sgguCd + ", page " + pageNo + ": " + e.getMessage());
					e.printStackTrace();
					hasMorePages = false;
				}
			}
		}

		System.out.println("Method finished: HospitalApiService.fetchParseAndSaveHospitals(), Result: "
				+ totalSavedOrUpdatedCount);
		return totalSavedOrUpdatedCount;
	}

	@Transactional(readOnly = true)
	@Override // HospitalMainService 인터페이스에 이 메서드가 선언되어 있어야 합니다.
	public List<Hospital> getAllHospitals() {
		// hospitalMainRepository.findAllHospitals() 대신 Spring Data JPA의 findAll() 사용
		return hospitalMainRepository.findAll();
		}
	

	@Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
	@Override // HospitalMainService 인터페이스에 이 메서드가 선언되어 있어야 합니다.
	public List<String> getAllHospitalCodes() {
		System.out.println("HospitalMainServiceImpl: Fetching all hospital codes from DB...");
		// HospitalMainRepository의 findAll() 메서드를 사용하여 모든 Hospital 엔티티를 가져온 후
		// Stream API를 사용하여 각 엔티티에서 ykiho 필드만 추출하여 List<String>으로 변환
		return hospitalMainRepository.findAll().stream().map(Hospital::getHospitalCode) // Hospital 엔티티에 getYkiho() 메서드가 있어야
																					// 합니다.
				.collect(Collectors.toList());
	}

}

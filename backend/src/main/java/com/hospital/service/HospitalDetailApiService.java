package com.hospital.service;

import com.hospital.client.HospitalDetailApiCaller; // ApiCaller 임포트
import com.hospital.dto.api.HospitalDetailApiResponse;
import com.hospital.entity.HospitalDetail;
import com.hospital.parser.HospitalDetailApiParser; // Parser 임포트
import com.hospital.repository.HospitalDetailRepository; // Repository 임포트 예정 (아직 없다면 다음 단계에서 생성)
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HospitalDetailApiService {

    private final HospitalDetailApiCaller hospitalDetailApiCaller;
    private final HospitalDetailApiParser hospitalDetailApiParser;
    private final HospitalDetailRepository hospitalDetailRepository;

    // 생성자 주입 (Spring이 빈으로 등록된 Caller, Parser, Repository를 자동으로 주입해줍니다)
    public HospitalDetailApiService(HospitalDetailApiCaller hospitalDetailApiCaller,
                                    HospitalDetailApiParser hospitalDetailApiParser,
                                    HospitalDetailRepository hospitalDetailRepository) {
        this.hospitalDetailApiCaller = hospitalDetailApiCaller;
        this.hospitalDetailApiParser = hospitalDetailApiParser;
        this.hospitalDetailRepository = hospitalDetailRepository;
    }

    /**
     * 특정 병원 코드에 해당하는 의료 상세 정보를 외부 API에서 가져와 DB에 저장합니다.
     * API 호출은 HospitalDetailApiCaller에게, DTO->엔티티 변환은 HospitalDetailApiParser에게 위임됩니다.
     *
     * @param hospitalCode 병원 코드 (ykiho 값)
     * @return 저장되거나 업데이트된 HospitalDetail 엔티티, 또는 null (API 호출 실패, 유효한 데이터 없음 등)
     */
    @Transactional // DB 작업에 대한 트랜잭션 처리
    public HospitalDetail fetchAndSaveHospitalDetail(String hospitalCode) {
        HospitalDetailApiResponse apiResponse;
        try {
            // 1. API 호출: ApiCaller에게 API 호출을 위임하고 API 응답 DTO를 받습니다.
            apiResponse = hospitalDetailApiCaller.callApi(hospitalCode);
        } catch (RuntimeException e) {
            System.err.println("Error calling HospitalDetail API for code " + hospitalCode + ": " + e.getMessage());
            // API 호출 실패 시, 서비스 계층에서 적절히 처리 (예: null 반환, 커스텀 예외 던지기)
            return null;
        }

        HospitalDetail hospitalDetail = null;
        if (apiResponse != null) {
            // 2. 데이터 파싱 및 엔티티 변환: ApiParser에게 DTO -> 엔티티 변환을 위임합니다.
            hospitalDetail = hospitalDetailApiParser.parseHospitalDetail(apiResponse);
        }

        if (hospitalDetail != null) {
            // 3. DB 저장 또는 업데이트
            // 해당 병원 코드의 상세 정보가 이미 DB에 존재하는지 확인 (PK 기준)
            Optional<HospitalDetail> existingDetail = hospitalDetailRepository.findById(hospitalDetail.getHospitalCode());

            if (existingDetail.isPresent()) {
                // 기존 데이터가 있다면 업데이트 (JPA 영속성 컨텍스트 활용)
                HospitalDetail updatedDetail = existingDetail.get();
                // 파서에서 변환된 최신 데이터로 필드들을 업데이트합니다.
                // 모든 필드를 하나씩 set 해주는 것이 가장 명확합니다.
                updatedDetail.setEmyDayYn(hospitalDetail.getEmyDayYn());
                updatedDetail.setEmyNightYn(hospitalDetail.getEmyNightYn());
                updatedDetail.setParkQty(hospitalDetail.getParkQty());
                updatedDetail.setLunchWeek(hospitalDetail.getLunchWeek());
                updatedDetail.setRcvWeek(hospitalDetail.getRcvWeek());
                updatedDetail.setRcvSat(hospitalDetail.getRcvSat());
                updatedDetail.setTrmtMonStart(hospitalDetail.getTrmtMonStart());
                updatedDetail.setTrmtMonEnd(hospitalDetail.getTrmtMonEnd());
                updatedDetail.setTrmtTueStart(hospitalDetail.getTrmtTueStart());
                updatedDetail.setTrmtTueEnd(hospitalDetail.getTrmtTueEnd());
                updatedDetail.setTrmtWedStart(hospitalDetail.getTrmtWedStart());
                updatedDetail.setTrmtWedEnd(hospitalDetail.getTrmtWedEnd());
                updatedDetail.setTrmtThurStart(hospitalDetail.getTrmtThurStart());
                updatedDetail.setTrmtThurEnd(hospitalDetail.getTrmtThurEnd());
                updatedDetail.setTrmtFriStart(hospitalDetail.getTrmtFriStart());
                updatedDetail.setTrmtFriEnd(hospitalDetail.getTrmtFriEnd());

                // 변경된 엔티티를 저장 (JPA는 변경 감지를 통해 자동으로 UPDATE 쿼리 실행)
                return hospitalDetailRepository.save(updatedDetail);
            } else {
                // 기존 데이터가 없다면 새로운 엔티티 저장
                return hospitalDetailRepository.save(hospitalDetail);
            }
        } else {
            System.out.println("No valid HospitalDetail entity created or found for hospital code: " + hospitalCode + ". Skipping DB save.");
            return null; // 변환된 엔티티가 없거나 유효하지 않으면 null 반환
        }
    }

    /**
     * 여러 병원 코드 리스트에 대해 fetchAndSaveHospitalDetail 메서드를 반복 호출합니다.
     * 이 메서드는 예를 들어, HospitalMainService에서 전체 병원 코드 목록을 가져온 후
     * 각 병원의 상세 정보를 업데이트할 때 사용될 수 있습니다.
     *
     * @param hospitalCodes 상세 정보를 가져와 저장할 병원 코드들의 리스트
     */
    @Transactional // 여러 상세 정보 저장이 하나의 트랜잭션으로 묶이도록 할 수 있습니다. (개별 호출에도 트랜잭션이 있지만, 전체적으로 묶기 위함)
    public void fetchAndSaveAllHospitalDetails(List<String> hospitalCodes) {
        if (hospitalCodes == null || hospitalCodes.isEmpty()) {
            System.out.println("No hospital codes provided to fetch details. Skipping batch processing.");
            return;
        }
        System.out.println("Starting to fetch and save details for " + hospitalCodes.size() + " hospitals...");
        for (String code : hospitalCodes) {
            System.out.println("  Processing detail for hospital code: " + code);
            fetchAndSaveHospitalDetail(code); // 각 병원 코드에 대해 상세 정보 가져와 저장
            // API 호출 빈도를 조절하기 위해 Thread.sleep()을 추가하는 것을 고려하세요.
            // 공공 API는 호출 제한이 있는 경우가 많으므로 너무 빠른 호출은 API 차단을 유발할 수 있습니다.
            // 예: try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        System.out.println("Finished fetching and saving all hospital details.");
    }
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
    public List<HospitalDetail> getAllHospitalDetails() {
        System.out.println("HospitalDetailApiService: Fetching all HospitalDetails from repository...");
        return hospitalDetailRepository.findAll(); // JpaRepository의 findAll() 사용
    }
}
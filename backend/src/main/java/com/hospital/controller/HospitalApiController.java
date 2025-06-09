package com.hospital.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.entity.Hospital;
import com.hospital.entity.HospitalDetail;
import com.hospital.service.HospitalDetailApiService;
import com.hospital.service.HospitalMainService;


@RestController
@RequestMapping("/api/hospitals") 
public class HospitalApiController {

    private final HospitalMainService hospitalMainService;
    private final HospitalDetailApiService hospitalDetailApiService; // HospitalDetailApiService 주입을 위해 추가

    public HospitalApiController(HospitalMainService hospitalMainService, HospitalDetailApiService hospitalDetailApiService) {
        this.hospitalMainService = hospitalMainService;
        this.hospitalDetailApiService =hospitalDetailApiService;
    }

    @GetMapping(value = "/save", produces = MediaType.TEXT_PLAIN_VALUE)
    public String saveHospitalsToDb() {
        int savedCount = 0;
        try {
            System.out.println("Starting to fetch, parse, and save hospitals to DB...");
            savedCount = hospitalMainService.fetchParseAndSaveHospitals();
            return "병원 정보 " + savedCount + "개 DB 저장 완료!";
        } catch (Exception e) {
            System.err.println("Error occurred during DB save: " + e.getMessage());
            return "병원 정보 DB 저장 중 오류 발생: " + e.getMessage();
        }
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Hospital> getAllHospitals() {
        System.out.println("Fetching all hospitals from DB...");
        return hospitalMainService.getAllHospitals();
    }
    @GetMapping(value = "/detail/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public HospitalDetail saveHospitalDetailToDb(@RequestParam("hospitalCode") String hospitalCode) {
        System.out.println("Starting to fetch, parse, and save detail for hospital code: " + hospitalCode);
        try {
            HospitalDetail savedDetail = hospitalDetailApiService.fetchAndSaveHospitalDetail(hospitalCode);
            if (savedDetail != null) {
                System.out.println("HospitalDetail for " + hospitalCode + " saved/updated successfully.");
                return savedDetail;
            } else {
                System.out.println("No detail found or saved for hospital code: " + hospitalCode);
                // 적절한 응답 (예: 404 Not Found 또는 빈 객체)을 반환할 수 있습니다.
                return null; // 또는 throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Detail not found");
            }
        } catch (Exception e) {
            System.err.println("Error occurred during HospitalDetail DB save for " + hospitalCode + ": " + e.getMessage());
            throw new RuntimeException("병원 상세 정보 저장 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * DB에 저장된 모든 병원 상세 정보를 조회합니다.
     * (이 기능을 사용하려면 HospitalDetailApiService에 getAllHospitalDetails() 메서드를 추가해야 합니다.)
     * 예: GET /api/hospitals/detail/list
     *
     * @return DB에 저장된 모든 HospitalDetail 엔티티 리스트 (JSON)
     */
    @GetMapping(value = "/detail/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HospitalDetail> getAllHospitalDetails() {
        System.out.println("Fetching all hospital details from DB...");
        // 이 메서드는 HospitalDetailApiService에 구현이 필요합니다.
        // 예: public List<HospitalDetail> getAllHospitalDetails() { return hospitalDetailRepository.findAll(); }
        // return hospitalDetailApiService.getAllHospitalDetails(); // 이 줄의 주석을 해제하려면 서비스 메서드 구현 필수

        System.err.println("getAllHospitalDetails API는 HospitalDetailApiService에 구현이 필요합니다.");
        return List.of(); // 임시로 빈 리스트 반환
    }
    @GetMapping(value = "/detail/save-all", produces = MediaType.TEXT_PLAIN_VALUE)
    public String saveAllHospitalDetailsFromMainDb() {
        System.out.println("Starting to fetch all hospital codes from main DB and save their details...");
        try {
            // 1. HospitalMainService에서 모든 병원 코드(ykiho)를 가져옵니다.
            List<String> hospitalCodes = hospitalMainService.getAllHospitalCodes();

            if (hospitalCodes.isEmpty()) {
                return "메인 DB에 저장된 병원 코드가 없습니다. 먼저 /api/hospitals/save를 통해 메인 병원 정보를 저장하세요.";
            }

            System.out.println("Found " + hospitalCodes.size() + " hospital codes from main DB. Proceeding to fetch details.");

            // 2. HospitalDetailApiService에 모든 병원 코드를 전달하여 상세 정보 저장 로직을 시작합니다.
            hospitalDetailApiService.fetchAndSaveAllHospitalDetails(hospitalCodes);

            return "메인 DB의 모든 병원 상세 정보 저장이 요청되었습니다. (백그라운드에서 처리될 수 있습니다)";
        } catch (Exception e) {
            System.err.println("Error occurred during batch HospitalDetail save: " + e.getMessage());
            return "모든 병원 상세 정보 저장 중 오류 발생: " + e.getMessage();
        }
    }
}

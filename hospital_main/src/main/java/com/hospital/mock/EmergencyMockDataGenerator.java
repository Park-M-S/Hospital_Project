package com.hospital.mock;

import com.hospital.dto.EmergencyWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class EmergencyMockDataGenerator {

    private List<EmergencyWebResponse> cachedData = new ArrayList<>();
    private final Random random = new Random();
    private boolean schedulerEnabled = true; // 🔥 스케줄러 제어 플래그

    // 성남시 범위
    private static final CoordinateRange SEONGNAM_BOUNDS = new CoordinateRange(
            37.4000, 37.4600, 127.1200, 127.1600
    );

    /**
     * 캐시된 응급실 데이터 조회
     */
    public List<EmergencyWebResponse> getCachedEmergencyData() {
        if (cachedData.isEmpty()) {
            generateFixedHospitals();
        }

        // 스케줄러 비활성화 상태면 캐시된 데이터 그대로 반환
        if (!schedulerEnabled) {
            return cachedData;
        }

        // 병상 수, 장비, lastUpdatedDate만 갱신
        List<EmergencyWebResponse> updatedData = new ArrayList<>();
        for (EmergencyWebResponse hospital : cachedData) {
            updatedData.add(EmergencyWebResponse.builder()
                    .hpid(hospital.getHpid())
                    .dutyName(hospital.getDutyName())
                    .dutyTel3(hospital.getDutyTel3())
                    .emergencyBeds(randomInt(5, 10))
                    .operatingBeds(randomInt(2, 7))
                    .generalWardBeds(randomInt(10, 30))
                    .ambulanceAvailability(random.nextBoolean())
                    .ventilatorAvailability(random.nextBoolean())
                    .ctAvailability(random.nextBoolean())
                    .mriAvailability(random.nextBoolean())
                    .crrtAvailability(random.nextBoolean())
                    .lastUpdatedDate(getCurrentDateTime())
                    .coordinateX(hospital.getCoordinateX())
                    .coordinateY(hospital.getCoordinateY())
                    .emergencyAddress(hospital.getEmergencyAddress())
                    .build());
        }

        return updatedData;
    }

    /**
     * 5개 병원 초기 생성 (이름, 전화번호, 주소, 좌표 고정)
     */
    private void generateFixedHospitals() {
        List<EmergencyWebResponse> hospitals = new ArrayList<>();

        // 1. 성남시 의료원 (좌표 고정)
        hospitals.add(EmergencyWebResponse.builder()
                .hpid("H001")
                .dutyName("성남시 의료원")
                .dutyTel3("031-1234-5678")
                .emergencyBeds(0)
                .operatingBeds(0)
                .generalWardBeds(0)
                .ambulanceAvailability(false)
                .ventilatorAvailability(false)
                .ctAvailability(false)
                .mriAvailability(false)
                .crrtAvailability(false)
                .lastUpdatedDate(getCurrentDateTime())
                .coordinateX(37.4367)
                .coordinateY(127.1387)
                .emergencyAddress("성남시 수정구 태평로 123")
                .build()
        );

        // 2~5. 성남시 내 다른 병원 (랜덤 좌표, 나머지 고정)
        hospitals.add(EmergencyWebResponse.builder()
                .hpid("H002")
                .dutyName("성남시 병원 2")
                .dutyTel3("031-2345-6789")
                .emergencyBeds(0)
                .operatingBeds(0)
                .generalWardBeds(0)
                .ambulanceAvailability(false)
                .ventilatorAvailability(false)
                .ctAvailability(false)
                .mriAvailability(false)
                .crrtAvailability(false)
                .lastUpdatedDate(getCurrentDateTime())
                .coordinateX(SEONGNAM_BOUNDS.minLat + Math.random() * (SEONGNAM_BOUNDS.maxLat - SEONGNAM_BOUNDS.minLat))
                .coordinateY(SEONGNAM_BOUNDS.minLng + Math.random() * (SEONGNAM_BOUNDS.maxLng - SEONGNAM_BOUNDS.minLng))
                .emergencyAddress("성남시 수정구 랜덤로 20")
                .build());

        hospitals.add(EmergencyWebResponse.builder()
                .hpid("H003")
                .dutyName("성남시 병원 3")
                .dutyTel3("031-3456-7890")
                .emergencyBeds(0)
                .operatingBeds(0)
                .generalWardBeds(0)
                .ambulanceAvailability(false)
                .ventilatorAvailability(false)
                .ctAvailability(false)
                .mriAvailability(false)
                .crrtAvailability(false)
                .lastUpdatedDate(getCurrentDateTime())
                .coordinateX(SEONGNAM_BOUNDS.minLat + Math.random() * (SEONGNAM_BOUNDS.maxLat - SEONGNAM_BOUNDS.minLat))
                .coordinateY(SEONGNAM_BOUNDS.minLng + Math.random() * (SEONGNAM_BOUNDS.maxLng - SEONGNAM_BOUNDS.minLng))
                .emergencyAddress("성남시 수정구 랜덤로 30")
                .build());

        hospitals.add(EmergencyWebResponse.builder()
                .hpid("H004")
                .dutyName("성남시 병원 4")
                .dutyTel3("031-4567-8901")
                .emergencyBeds(0)
                .operatingBeds(0)
                .generalWardBeds(0)
                .ambulanceAvailability(false)
                .ventilatorAvailability(false)
                .ctAvailability(false)
                .mriAvailability(false)
                .crrtAvailability(false)
                .lastUpdatedDate(getCurrentDateTime())
                .coordinateX(SEONGNAM_BOUNDS.minLat + Math.random() * (SEONGNAM_BOUNDS.maxLat - SEONGNAM_BOUNDS.minLat))
                .coordinateY(SEONGNAM_BOUNDS.minLng + Math.random() * (SEONGNAM_BOUNDS.maxLng - SEONGNAM_BOUNDS.minLng))
                .emergencyAddress("성남시 수정구 랜덤로 40")
                .build());

        hospitals.add(EmergencyWebResponse.builder()
                .hpid("H005")
                .dutyName("성남시 병원 5")
                .dutyTel3("031-5678-9012")
                .emergencyBeds(0)
                .operatingBeds(0)
                .generalWardBeds(0)
                .ambulanceAvailability(false)
                .ventilatorAvailability(false)
                .ctAvailability(false)
                .mriAvailability(false)
                .crrtAvailability(false)
                .lastUpdatedDate(getCurrentDateTime())
                .coordinateX(SEONGNAM_BOUNDS.minLat + Math.random() * (SEONGNAM_BOUNDS.maxLat - SEONGNAM_BOUNDS.minLat))
                .coordinateY(SEONGNAM_BOUNDS.minLng + Math.random() * (SEONGNAM_BOUNDS.maxLng - SEONGNAM_BOUNDS.minLng))
                .emergencyAddress("성남시 수정구 랜덤로 50")
                .build());

        cachedData = hospitals;
        log.info("5개 성남시 병원 초기 데이터 생성 완료");
    }

    private static int randomInt(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    private static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 스케줄러 제어 메서드
     */
    public void disableScheduler() {
        this.schedulerEnabled = false;
        log.info("MockDataGenerator 스케줄러 비활성화됨");
    }

    public void enableScheduler() {
        this.schedulerEnabled = true;
        log.info("MockDataGenerator 스케줄러 활성화됨");
    }

    /**
     * 좌표 범위 클래스
     */
    private static class CoordinateRange {
        double minLat, maxLat, minLng, maxLng;

        public CoordinateRange(double minLat, double maxLat, double minLng, double maxLng) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLng = minLng;
            this.maxLng = maxLng;
        }
    }
}

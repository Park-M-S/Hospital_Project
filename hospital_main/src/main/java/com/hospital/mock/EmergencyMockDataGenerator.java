package com.hospital.mock;

import com.hospital.dto.EmergencyWebResponse;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class EmergencyMockDataGenerator {

    private List<EmergencyWebResponse> cachedData = new ArrayList<>();
    private List<EmergencyWebResponse> staticHospitalData = new ArrayList<>(); // 고정 병원 정보
    private final Random random = new Random();
    private boolean schedulerEnabled = false;
    
    // 지역별 응급실 분포 (총 300개로 수정)
    private static final Map<String, Integer> REGIONAL_DISTRIBUTION;
    static {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("서울", 52);
        distribution.put("경기", 43);
        distribution.put("부산", 25);
        distribution.put("대구", 20);
        distribution.put("인천", 18);
        distribution.put("광주", 15);
        distribution.put("대전", 15);
        distribution.put("울산", 12);
        distribution.put("강원", 20);
        distribution.put("충북", 15);
        distribution.put("충남", 18);
        distribution.put("전북", 15);
        distribution.put("전남", 18);
        distribution.put("경북", 20);
        distribution.put("경남", 22);
        distribution.put("제주", 8);
        distribution.put("세종", 4);
        REGIONAL_DISTRIBUTION = Collections.unmodifiableMap(distribution);
    }
    
    // 시도별 좌표 범위
    private static final Map<String, CoordinateRange> REGIONAL_BOUNDS;
    static {
        Map<String, CoordinateRange> bounds = new HashMap<>();
        bounds.put("서울", new CoordinateRange(37.4500, 37.7000, 126.7500, 127.2000));
        bounds.put("부산", new CoordinateRange(35.0000, 35.3500, 128.9000, 129.3500));
        bounds.put("대구", new CoordinateRange(35.7000, 35.9500, 128.4500, 128.7500));
        bounds.put("인천", new CoordinateRange(37.2500, 37.6500, 126.4000, 126.9000));
        bounds.put("광주", new CoordinateRange(35.0500, 35.2500, 126.7000, 127.0000));
        bounds.put("대전", new CoordinateRange(36.2000, 36.5000, 127.2000, 127.5500));
        bounds.put("울산", new CoordinateRange(35.4000, 35.7000, 129.0000, 129.4500));
        bounds.put("경기", new CoordinateRange(36.8000, 38.3000, 126.4000, 127.9000));
        bounds.put("강원", new CoordinateRange(37.0000, 38.6000, 127.0000, 129.4000));
        bounds.put("충북", new CoordinateRange(36.0000, 37.0000, 127.2000, 128.5000));
        bounds.put("충남", new CoordinateRange(35.9000, 37.0000, 126.1000, 127.8000));
        bounds.put("전북", new CoordinateRange(35.1000, 36.0000, 126.4000, 127.8000));
        bounds.put("전남", new CoordinateRange(34.2000, 35.7000, 125.0000, 127.6000));
        bounds.put("경북", new CoordinateRange(35.4000, 37.5000, 128.0000, 129.9000));
        bounds.put("경남", new CoordinateRange(34.6000, 35.9000, 127.4000, 129.3000));
        bounds.put("제주", new CoordinateRange(33.1000, 33.6000, 126.1000, 126.9000));
        bounds.put("세종", new CoordinateRange(36.4000, 36.6000, 127.1000, 127.4000));
        REGIONAL_BOUNDS = Collections.unmodifiableMap(bounds);
    }
    
    // 병원명 구성 요소
    private static final String[] HOSPITAL_PREFIXES = {
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "경기", "강원",
        "삼성", "한양", "고려", "연세", "가톨릭", "아주", "인하", "순천향",
        "중앙", "성모", "세브란스", "신촌", "강남", "분당", "일산"
    };
    
    private static final String[] HOSPITAL_TYPES = {
        "대학교병원", "대학병원", "의료원", "병원", "종합병원", "의료센터"
    };
    
    private static final String[] PHONE_PREFIXES = {
        "02", "031", "032", "033", "041", "042", "043", "051", "052", "053", "054", "055", "061", "062", "063", "064"
    };

    /**
     * 초기 병원 기본 정보 생성 (한 번만 실행)
     */
    @PostConstruct
    private void initializeStaticHospitalData() {
        List<EmergencyWebResponse> staticData = new ArrayList<>();
        int hospitalId = 1;
        
        // 지역별로 배정된 수만큼 기본 정보 생성
        for (Map.Entry<String, Integer> entry : REGIONAL_DISTRIBUTION.entrySet()) {
            String region = entry.getKey();
            int count = entry.getValue();
            
            for (int i = 0; i < count; i++) {
                staticData.add(generateStaticHospitalInfo(region, hospitalId++));
            }
        }
        
        staticHospitalData = staticData;
        log.info("고정 병원 데이터 초기화 완료: {}건", staticData.size());
    }
    
    /**
     * 병원 기본 정보 생성 (고정값)
     */
    private EmergencyWebResponse generateStaticHospitalInfo(String region, int hospitalId) {
        CoordinateRange bounds = REGIONAL_BOUNDS.get(region);
        
        EmergencyWebResponse response = EmergencyWebResponse.builder()
            .hpid("H" + String.format("%03d", hospitalId))
            .dutyName(generateRegionalHospitalName(region))
            .dutyTel3(generateRandomPhoneNumber(region))
            .lastUpdatedDate(getCurrentDateTime())
            .build();
            
        // 좌표 설정 (고정)
        response.setCoordinates(
        	    generateRandomLongitude(bounds),  // 경도를 첫 번째로
        	    generateRandomLatitude(bounds)    // 위도를 두 번째로
        	);
        
        // 주소 설정 (고정)
        response.setEmergencyAddress(generateRegionalAddress(region));
        
        return response;
    }

    /**
     * 30초마다 응급실 동적 데이터만 갱신
     */
    @Scheduled(fixedRate = 30000)
    public void generateRandomEmergencyData() {
        // 스케줄러가 비활성화되어 있으면 실행하지 않음
        if (!schedulerEnabled) {
            return;
        }
        
        log.info("응급실 동적 데이터 갱신 시작...");
        
        List<EmergencyWebResponse> newData = new ArrayList<>();
        
        for (EmergencyWebResponse staticHospital : staticHospitalData) {
            String region = getRegionFromHospitalName(staticHospital.getDutyName());
            HospitalCharacteristics chars = getRegionalCharacteristics(region);
            
            // 기본 정보 복사 + 동적 데이터만 새로 생성
            EmergencyWebResponse dynamicHospital = EmergencyWebResponse.builder()
                .hpid(staticHospital.getHpid())
                .dutyName(staticHospital.getDutyName())
                .dutyTel3(staticHospital.getDutyTel3())
                // 동적 데이터만 새로 생성
                .emergencyBeds(generateRandomBedCount("emergency", chars))
                .operatingBeds(generateRandomBedCount("operating", chars))
                .generalWardBeds(generateRandomBedCount("general", chars))
                .ambulanceAvailability(randomBoolean(chars.ambulanceRate))
                .ventilatorAvailability(randomBoolean(chars.ventilatorRate))
                .ctAvailability(randomBoolean(chars.ctRate))
                .mriAvailability(randomBoolean(chars.mriRate))
                .crrtAvailability(randomBoolean(chars.crrtRate))
                .lastUpdatedDate(getCurrentDateTime())
                .build();
            
            // 고정 정보 복사
            dynamicHospital.setCoordinates(staticHospital.getCoordinateX(), staticHospital.getCoordinateY());
            dynamicHospital.setEmergencyAddress(staticHospital.getEmergencyAddress());
            
            newData.add(dynamicHospital);
        }
        
        cachedData = newData;
        log.info("응급실 동적 데이터 갱신 완료: {}건", newData.size());
    }
    
    /**
     * 병원명에서 지역 추출
     */
    private String getRegionFromHospitalName(String hospitalName) {
        for (String region : REGIONAL_DISTRIBUTION.keySet()) {
            if (hospitalName.contains(region)) {
                return region;
            }
        }
        return "서울"; // 기본값
    }
    
    /**
     * 스케줄러 활성화 (엔드포인트에서 호출)
     */
    public void enableScheduler() {
        schedulerEnabled = true;
        // 즉시 한 번 실행
        generateRandomEmergencyData();
        log.info("Mock 데이터 스케줄러 활성화됨");
    }
    
    /**
     * 스케줄러 비활성화
     */
    public void disableScheduler() {
        schedulerEnabled = false;
        log.info("Mock 데이터 스케줄러 비활성화됨");
    }
    
    /**
     * 스케줄러 상태 확인
     */
    public boolean isSchedulerEnabled() {
        return schedulerEnabled;
    }
    
    /**
     * 캐시된 응급실 데이터 조회
     */
    public List<EmergencyWebResponse> getCachedEmergencyData() {
        if (cachedData.isEmpty()) {
            generateRandomEmergencyData(); // 최초 1회 생성
        }
        return new ArrayList<>(cachedData); // 방어적 복사
    }
    
    /**
     * 지역별 병원 특성 반환
     */
    private HospitalCharacteristics getRegionalCharacteristics(String region) {
        return switch(region) {
            case "서울" -> new HospitalCharacteristics(0.85, 0.90, 0.95, 0.80, 0.70); // 서울 - 높은 장비 보유율
            case "부산", "대구", "광주", "대전", "인천" -> new HospitalCharacteristics(0.80, 0.85, 0.90, 0.70, 0.60);
            case "경기", "울산" -> new HospitalCharacteristics(0.75, 0.80, 0.85, 0.65, 0.55);
            default -> new HospitalCharacteristics(0.70, 0.75, 0.80, 0.60, 0.45); // 지방 - 상대적으로 낮은 보유율
        };
    }
    
    /**
     * 지역별 병원명 생성
     */
    private String generateRegionalHospitalName(String region) {
        String prefix = Math.random() < 0.6 ? region : HOSPITAL_PREFIXES[random.nextInt(HOSPITAL_PREFIXES.length)];
        String type = HOSPITAL_TYPES[random.nextInt(HOSPITAL_TYPES.length)];
        return prefix + type;
    }
    
    /**
     * 지역별 전화번호 생성
     */
    private String generateRandomPhoneNumber(String region) {
        String areaCode = getAreaCode(region);
        String number = String.format("%04d-%04d", 
            random.nextInt(9000) + 1000, 
            random.nextInt(9000) + 1000);
        return areaCode + "-" + number;
    }
    
    /**
     * 지역별 지역번호 반환
     */
    private String getAreaCode(String region) {
        return switch(region) {
            case "서울" -> "02";
            case "경기", "인천" -> "031";
            case "부산" -> "051";
            case "대구" -> "053";
            case "광주" -> "062";
            case "대전" -> "042";
            case "울산" -> "052";
            case "강원" -> "033";
            case "충북" -> "043";
            case "충남", "세종" -> "041";
            case "전북" -> "063";
            case "전남" -> "061";
            case "경북" -> "054";
            case "경남" -> "055";
            case "제주" -> "064";
            default -> "02";
        };
    }
    
    /**
     * 병상 수 생성 (음수 = 포화상태)
     */
    private Integer generateRandomBedCount(String bedType, HospitalCharacteristics chars) {
        int baseBeds = switch(bedType) {
            case "emergency" -> ThreadLocalRandom.current().nextInt(5, 25);
            case "operating" -> ThreadLocalRandom.current().nextInt(2, 12);
            case "general" -> ThreadLocalRandom.current().nextInt(10, 50);
            default -> 10;
        };

        // 시간대별 포화도 적용 + 랜덤 변동
        double loadFactor = getCurrentLoadFactor();
        double randomVariation = (Math.random() - 0.5) * 0.4; // ±20% 랜덤 변동
        double totalLoad = loadFactor * chars.busyFactor + randomVariation;

        // 포화 상황에서는 음수도 가능 (대기 환자 수)
        int occupiedBeds = (int)(baseBeds * totalLoad);
        int availableBeds = baseBeds - occupiedBeds;

        // 포화시 음수 범위 제한 (너무 큰 음수는 비현실적)
        if (availableBeds < 0) {
            availableBeds = Math.max(availableBeds, -baseBeds / 2); // 기본 병상의 절반까지만 초과
        }

        return availableBeds;
    }
    
    /**
     * 현재 시간대별 부하 계수 계산 (1.0 이상이면 포화 가능)
     */
    private double getCurrentLoadFactor() {
        int hour = LocalDateTime.now().getHour();
        return switch(hour) {
            case 0, 1, 2, 3, 4, 5 -> 0.4; // 새벽 여유
            case 6, 7, 8, 9, 10, 11 -> 0.7; // 오전 보통
            case 12, 13, 14, 15, 16, 17 -> 1.0; // 오후 혼잡
            case 18, 19, 20, 21, 22, 23 -> 1.3; // 저녁 포화 위험 (1.0 이상)
            default -> 0.8;
        };
    }
    
    /**
     * Boolean 랜덤 생성
     */
    private Boolean randomBoolean(double trueProbability) {
        return Math.random() < trueProbability;
    }
    
    /**
     * 랜덤 위도 생성
     */
    private Double generateRandomLatitude(CoordinateRange bounds) {
        return bounds.minLat + Math.random() * (bounds.maxLat - bounds.minLat);
    }
    
    /**
     * 랜덤 경도 생성
     */
    private Double generateRandomLongitude(CoordinateRange bounds) {
        return bounds.minLng + Math.random() * (bounds.maxLng - bounds.minLng);
    }
    
    /**
     * 지역별 주소 생성
     */
    private String generateRegionalAddress(String region) {
        String district = generateRandomDistrict(region);
        int roadNumber = random.nextInt(500) + 1;
        int buildingNumber = random.nextInt(100) + 1;
        return String.format("%s %s 의료로 %d-%d", region, district, roadNumber, buildingNumber);
    }
    
    /**
     * 지역별 구/군 생성
     */
    private String generateRandomDistrict(String region) {
        String[] districts = switch(region) {
            case "서울" -> new String[]{"강남구", "서초구", "종로구", "중구", "용산구", "성동구", "마포구", "영등포구"};
            case "부산" -> new String[]{"해운대구", "부산진구", "동래구", "남구", "중구", "서구", "사하구"};
            case "대구" -> new String[]{"중구", "동구", "서구", "남구", "북구", "수성구", "달서구"};
            case "경기" -> new String[]{"수원시", "성남시", "고양시", "용인시", "부천시", "안산시", "안양시"};
            default -> new String[]{region + "구", region + "시", region + "군"};
        };
        return districts[random.nextInt(districts.length)];
    }
    
    /**
     * 현재 시간 문자열 반환
     */
    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
    
    /**
     * 좌표 범위 클래스
     */
    @AllArgsConstructor
    private static class CoordinateRange {
        double minLat, maxLat, minLng, maxLng;
    }
    
    /**
     * 병원 특성 클래스
     */
    @AllArgsConstructor
    private static class HospitalCharacteristics {
        double ambulanceRate;    // 구급차 보유율
        double ventilatorRate;   // 인공호흡기 보유율
        double ctRate;          // CT 보유율
        double mriRate;         // MRI 보유율
        double crrtRate;        // CRRT 보유율
        double busyFactor = 0.7; // 혼잡도 (기본값)
        
        HospitalCharacteristics(double ambulanceRate, double ventilatorRate, double ctRate, double mriRate, double crrtRate) {
            this.ambulanceRate = ambulanceRate;
            this.ventilatorRate = ventilatorRate;
            this.ctRate = ctRate;
            this.mriRate = mriRate;
            this.crrtRate = crrtRate;
            this.busyFactor = 0.7;
        }
    }
}
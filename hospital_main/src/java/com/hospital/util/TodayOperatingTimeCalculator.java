package com.hospital.util;

import com.hospital.entity.HospitalDetail;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class TodayOperatingTimeCalculator {
    
    // 요일별 한글 이름 매핑
    private static final Map<DayOfWeek, String> DAY_OF_WEEK_KOREAN = new HashMap<>();
    static {
        DAY_OF_WEEK_KOREAN.put(DayOfWeek.MONDAY, "월요일");
        DAY_OF_WEEK_KOREAN.put(DayOfWeek.TUESDAY, "화요일");
        DAY_OF_WEEK_KOREAN.put(DayOfWeek.WEDNESDAY, "수요일");
        DAY_OF_WEEK_KOREAN.put(DayOfWeek.THURSDAY, "목요일");
        DAY_OF_WEEK_KOREAN.put(DayOfWeek.FRIDAY, "금요일");
        DAY_OF_WEEK_KOREAN.put(DayOfWeek.SATURDAY, "토요일");
        DAY_OF_WEEK_KOREAN.put(DayOfWeek.SUNDAY, "일요일");
    }
    
    public static TodayOperatingTime getTodayOperatingTime(HospitalDetail detail) {
        DayOfWeek today = CurrentTimeUtils.getCurrentDayOfWeek();
        String dayOfWeekKorean = getDayOfWeekKorean(today);
        
        if (detail == null) {
            return new TodayOperatingTime(null, null, dayOfWeekKorean);
        }
        
        OperatingHours hours = getOperatingHoursByDayOfWeek(detail, today);
        return new TodayOperatingTime(hours.openTime, hours.closeTime, dayOfWeekKorean);
    }
    
    /**
     * 요일에 따른 운영시간 조회
     */
    private static OperatingHours getOperatingHoursByDayOfWeek(HospitalDetail detail, DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return new OperatingHours(detail.getTrmtMonStart(), detail.getTrmtMonEnd());
            case TUESDAY:
                return new OperatingHours(detail.getTrmtTueStart(), detail.getTrmtTueEnd());
            case WEDNESDAY:
                return new OperatingHours(detail.getTrmtWedStart(), detail.getTrmtWedEnd());
            case THURSDAY:
                return new OperatingHours(detail.getTrmtThurStart(), detail.getTrmtThurEnd());
            case FRIDAY:
                return new OperatingHours(detail.getTrmtFriStart(), detail.getTrmtFriEnd());
            case SATURDAY:
                return new OperatingHours(detail.getTrmtSatStart(), detail.getTrmtSatEnd());
            case SUNDAY:
                return new OperatingHours(detail.getTrmtSunStart(), detail.getTrmtSunEnd());
            default:
                return new OperatingHours(null, null);
        }
    }
    
    /**
     * 요일을 한글로 변환
     */
    private static String getDayOfWeekKorean(DayOfWeek dayOfWeek) {
        return DAY_OF_WEEK_KOREAN.getOrDefault(dayOfWeek, "알 수 없음");
    }
    
    /**
     * 운영시간 정보를 담는 내부 클래스
     */
    @Getter
    @AllArgsConstructor
    private static class OperatingHours {
        private final String openTime;
        private final String closeTime;
    }
    
    /**
     * 오늘의 운영시간 정보
     */
    @Getter
    @AllArgsConstructor
    public static class TodayOperatingTime {
        private final String openTime;
        private final String closeTime;
        private final String dayOfWeekKorean;
    }
}
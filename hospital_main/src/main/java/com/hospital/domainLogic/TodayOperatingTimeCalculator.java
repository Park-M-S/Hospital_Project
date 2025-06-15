package com.hospital.domainLogic;

import com.hospital.entity.HospitalDetail;
import com.hospital.util.CurrentTimeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;

public class TodayOperatingTimeCalculator {
    
    public static TodayOperatingTime getTodayOperatingTime(HospitalDetail detail) {
        if (detail == null) {
            return new TodayOperatingTime(null, null, getDayOfWeekKorean(CurrentTimeUtils.getCurrentDayOfWeek()));
        }
        
        DayOfWeek today = CurrentTimeUtils.getCurrentDayOfWeek();
        
        String openTime = null;
        String closeTime = null;
        
        switch (today) {
            case MONDAY:
                openTime = detail.getTrmtMonStart();
                closeTime = detail.getTrmtMonEnd();
                break;
            case TUESDAY:
                openTime = detail.getTrmtTueStart();
                closeTime = detail.getTrmtTueEnd();
                break;
            case WEDNESDAY:
                openTime = detail.getTrmtWedStart();
                closeTime = detail.getTrmtWedEnd();
                break;
            case THURSDAY:
                openTime = detail.getTrmtThurStart();
                closeTime = detail.getTrmtThurEnd();
                break;
            case FRIDAY:
                openTime = detail.getTrmtFriStart();
                closeTime = detail.getTrmtFriEnd();
                break;
            case SATURDAY:
                openTime = detail.getTrmtSatStart();
                closeTime = detail.getTrmtSatEnd();
                break;
            case SUNDAY:
                openTime = detail.getTrmtSunStart();
                closeTime = detail.getTrmtSunEnd();
                break;
        }
        
        return new TodayOperatingTime(openTime, closeTime, getDayOfWeekKorean(today));
    }
    
    private static String getDayOfWeekKorean(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "월요일";
            case TUESDAY: return "화요일";
            case WEDNESDAY: return "수요일";
            case THURSDAY: return "목요일";
            case FRIDAY: return "금요일";
            case SATURDAY: return "토요일";
            case SUNDAY: return "일요일";
            default: return "알 수 없음";
        }
    }
    
    @Getter
    @AllArgsConstructor
    public static class TodayOperatingTime {
        private final String openTime;
        private final String closeTime;
        private final String dayOfWeekKorean;
    }
}
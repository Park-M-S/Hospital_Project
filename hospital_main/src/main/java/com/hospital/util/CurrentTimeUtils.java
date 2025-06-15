package com.hospital.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CurrentTimeUtils {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * 현재 시간을 HH:mm 형태로 반환 (기존 메서드)
     */
    public static String getCurrentTimeInHHMM() {
        LocalTime now = LocalTime.now();
        return now.format(TIME_FORMATTER);
    }
    
    /**
     * 현재 LocalDateTime 반환
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
    
    /**
     * 현재 LocalTime 반환
     */
    public static LocalTime getCurrentTime() {
        return LocalTime.now();
    }
    
    /**
     * 현재 요일 반환
     */
    public static DayOfWeek getCurrentDayOfWeek() {
        return LocalDateTime.now().getDayOfWeek();
    }
    
    /**
     * 현재가 평일인지 체크
     */
    public static boolean isCurrentlyWeekday() {
        DayOfWeek today = getCurrentDayOfWeek();
        return today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY;
    }
    
    /**
     * 현재가 주말인지 체크
     */
    public static boolean isCurrentlyWeekend() {
        return !isCurrentlyWeekday();
    }
    
    private CurrentTimeUtils() {
        // 인스턴스화를 막기 위한 private 생성자
    }
}
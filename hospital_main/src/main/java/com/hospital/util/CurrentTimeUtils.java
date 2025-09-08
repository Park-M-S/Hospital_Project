package com.hospital.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId; // ZoneId를 사용하기 위해 임포트합니다.
import java.time.format.DateTimeFormatter;

public class CurrentTimeUtils {

    // 한국 시간대(KST)를 상수로 정의합니다.
    // 병원 운영 시간이 KST 기준으로 입력되어 있다고 가정하므로 KST로 설정합니다.
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul"); 

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 현재 시간을 HH:mm 형태로 반환합니다.
     */
    public static String getCurrentTimeInHHMM() {
        // KOREA_ZONE_ID를 사용하여 현재 시간(LocalTime)을 가져옵니다.
        LocalTime now = LocalTime.now(KOREA_ZONE_ID);
        return now.format(TIME_FORMATTER);
    }

    /**
     * 현재 LocalDateTime을 반환합니다.
     */
    public static LocalDateTime getCurrentDateTime() {
        // KOREA_ZONE_ID를 사용하여 현재 LocalDateTime을 가져옵니다.
        return LocalDateTime.now(KOREA_ZONE_ID);
    }

    /**
     * 현재 LocalTime을 반환합니다.
     */
    public static LocalTime getCurrentTime() {
        // KOREA_ZONE_ID를 사용하여 현재 LocalTime을 가져옵니다.
        return LocalTime.now(KOREA_ZONE_ID);
    }

    /**
     * 현재 요일을 반환합니다.
     */
    public static DayOfWeek getCurrentDayOfWeek() {
        // KOREA_ZONE_ID를 사용하여 현재 LocalDateTime에서 요일을 가져옵니다.
        return LocalDateTime.now(KOREA_ZONE_ID).getDayOfWeek();
    }

    /**
     * 현재가 평일인지 체크합니다.
     */
    public static boolean isCurrentlyWeekday() {
        DayOfWeek today = getCurrentDayOfWeek();
        return today != DayOfWeek.SATURDAY && today != DayOfWeek.SUNDAY;
    }

    /**
     * 현재가 주말인지 체크합니다.
     */
    public static boolean isCurrentlyWeekend() {
        return !isCurrentlyWeekday();
    }

    private CurrentTimeUtils() {
        // 인스턴스화를 막기 위한 private 생성자입니다.
    }
}

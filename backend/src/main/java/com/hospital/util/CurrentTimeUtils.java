package com.hospital.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CurrentTimeUtils {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String getCurrentTimeInHHMM() {
        LocalTime now = LocalTime.now();
        return now.format(TIME_FORMATTER);
    }

    private CurrentTimeUtils() {
        //인스턴스화를 막기 위한 private 생성자
    }
}
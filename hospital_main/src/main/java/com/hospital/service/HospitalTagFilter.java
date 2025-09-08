package com.hospital.service;

import com.hospital.entity.HospitalMain;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.ProDoc;
import com.hospital.util.CurrentTimeUtils;
import com.hospital.util.TodayOperatingTimeCalculator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class HospitalTagFilter {

    public static boolean matchesAllTags(HospitalMain hospital, List<String> tags) {
        if (Objects.isNull(tags) || tags.isEmpty()) {
            return true;
        }

        for (String tag : tags) {
            switch (tag) {
            case "주차가능":
                if (Objects.isNull(hospital.getHospitalDetail()) || !hospital.getHospitalDetail().hasParkingSpace()) {
                    return false;
                }
                break;
            case "전문의":
                if (Objects.isNull(hospital.getProDocs()) || hospital.getProDocs().isEmpty()
                        || hospital.getProDocs().stream().noneMatch(ProDoc::hasSpecialist)) {
                    return false;
                }
                break;
            case "현재운영중":
                if (Objects.isNull(hospital.getHospitalDetail()) || !isCurrentlyOpen(hospital.getHospitalDetail())) {
                    return false;
                }
                break;
            //case "무료주차":
                //if (Objects.isNull(hospital.getHospitalDetail()) || !hospital.getHospitalDetail().hasParkingSpace()
                        //|| !hospital.getHospitalDetail().isFreeParking()) {
                    //return false;
                //}
                //break;
            case "토요일진료":
                if (Objects.isNull(hospital.getHospitalDetail())
                        || !hospital.getHospitalDetail().isSaturdayAvailable()) {
                    return false;
                }
                break;
            case "일요일진료":
                if (Objects.isNull(hospital.getHospitalDetail()) || !hospital.getHospitalDetail().isSundayAvailable()) {
                    return false;
                }
                break;
            default:
                if (Objects.isNull(hospital.getMedicalSubjects()) || hospital.getMedicalSubjects().isEmpty()
                        || hospital.getMedicalSubjects().stream().noneMatch(ms -> ms.getSubjectName().equals(tag))) {
                    return false;
                }
                break;
            }
        }
        return true;
    }

    private static boolean isCurrentlyOpen(HospitalDetail detail) {
        LocalDateTime now = CurrentTimeUtils.getCurrentDateTime();
        DayOfWeek today = now.getDayOfWeek();
        
        // TodayOperatingTimeCalculator의 로직 활용
        TodayOperatingTimeCalculator.TodayOperatingTime todayTime = 
            TodayOperatingTimeCalculator.getTodayOperatingTime(detail);
        
        String openTimeStr = todayTime.getOpenTime();
        String closeTimeStr = todayTime.getCloseTime();
        
        // 운영시간이 유효하지 않으면 운영하지 않음
        if (!HospitalDetail.isValidTime(openTimeStr) || !HospitalDetail.isValidTime(closeTimeStr)) {
            return false;
        }
        
        return isWithinOperatingHours(openTimeStr, closeTimeStr, now.toLocalTime());
    }

    /**
     * 현재 시간이 운영시간 내에 있는지 확인
     */
    private static boolean isWithinOperatingHours(String openTimeStr, String closeTimeStr, LocalTime currentTime) {
        try {
            LocalTime openTime = parseTimeString(openTimeStr);
            LocalTime closeTime = parseTimeString(closeTimeStr);
            
            // 자정을 넘어가는 경우 (예: 22:00 ~ 02:00)
            if (closeTime.isBefore(openTime)) {
                // 현재 시간이 오픈시간 이후이거나 마감시간 이전이면 운영중
                return currentTime.isAfter(openTime) || currentTime.equals(openTime) || 
                       currentTime.isBefore(closeTime);
            } else {
                // 일반적인 경우 (예: 09:00 ~ 18:00)
                return (currentTime.isAfter(openTime) || currentTime.equals(openTime)) && 
                       currentTime.isBefore(closeTime);
            }
        } catch (Exception e) {
            // 시간 파싱 실패 시 운영하지 않는 것으로 처리
            return false;
        }
    }
    
    /**
     * "HHmm" 형식의 문자열을 LocalTime으로 변환
     */
    private static LocalTime parseTimeString(String timeStr) {
        if (timeStr == null || timeStr.length() != 4) {
            throw new IllegalArgumentException("Invalid time format: " + timeStr);
        }
        
        int hour = Integer.parseInt(timeStr.substring(0, 2));
        int minute = Integer.parseInt(timeStr.substring(2, 4));
        
        // 시간 범위 검증
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Invalid time values: " + timeStr);
        }
        
        return LocalTime.of(hour, minute);
    }
}
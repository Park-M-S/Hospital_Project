package com.hospital.domainLogic;

import com.hospital.entity.HospitalMain;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.ProDoc;
import com.hospital.util.CurrentTimeUtils;

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
            case "무료주차":
                if (Objects.isNull(hospital.getHospitalDetail()) || !hospital.getHospitalDetail().hasParkingSpace()
                        || !hospital.getHospitalDetail().isFreeParking()) {
                    return false;
                }
                break;
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
        
        String openTimeStr = null;
        String closeTimeStr = null;
        
        // 요일별 운영시간 가져오기
        switch (today) {
            case MONDAY:
                openTimeStr = detail.getTrmtMonStart();
                closeTimeStr = detail.getTrmtMonEnd();
                break;
            case TUESDAY:
                openTimeStr = detail.getTrmtTueStart();
                closeTimeStr = detail.getTrmtTueEnd();
                break;
            case WEDNESDAY:
                openTimeStr = detail.getTrmtWedStart();
                closeTimeStr = detail.getTrmtWedEnd();
                break;
            case THURSDAY:
                openTimeStr = detail.getTrmtThurStart();
                closeTimeStr = detail.getTrmtThurEnd();
                break;
            case FRIDAY:
                openTimeStr = detail.getTrmtFriStart();
                closeTimeStr = detail.getTrmtFriEnd();
                break;
            case SATURDAY:
                openTimeStr = detail.getTrmtSatStart();
                closeTimeStr = detail.getTrmtSatEnd();
                break;
            case SUNDAY:
                openTimeStr = detail.getTrmtSunStart();
                closeTimeStr = detail.getTrmtSunEnd();
                break;
        }
        
        // 운영시간이 유효하지 않으면 운영하지 않음
        if (!isValidTime(openTimeStr) || !isValidTime(closeTimeStr)) {
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

    /**
     * 유효한 시간 값인지 체크
     */
    private static boolean isValidTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty() || "(NULL)".equals(timeStr)) {
            return false;
        }
        
        // 4자리 숫자 형식 체크
        if (!timeStr.matches("\\d{4}")) {
            return false;
        }
        
        // 0000은 운영하지 않는 시간으로 처리
        if ("0000".equals(timeStr)) {
            return false;
        }
        
        try {
            // 실제 시간 값 유효성 검증
            parseTimeString(timeStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
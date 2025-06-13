package com.hospital.domainLogic;

import com.hospital.entity.HospitalMain;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.MedicalSubject;
import com.hospital.entity.ProDoc;
import com.hospital.util.CurrentTimeUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class HospitalTagFilter {

    public static boolean matchesAllTags(HospitalMain hospital, List<String> tags) {
        // 태그 목록이 null이거나 비어있으면 필터링 없이 true 반환
        if (Objects.isNull(tags) || tags.isEmpty()) {
            return true;
        }

        // 각 태그에 대해 순회하며 조건을 확인
        for (String tag : tags) {
            switch (tag) {
                case "응급실":
                    if (Objects.isNull(hospital.getHospitalDetail()) || 
                        !hospital.getHospitalDetail().hasEmergencyService()) {
                        return false;
                    }
                    break;
                case "주차가능":
                    if (Objects.isNull(hospital.getHospitalDetail()) || 
                        !hospital.getHospitalDetail().hasParkingSpace()) {
                        return false;
                    }
                    break;
                case "전문의":
                    if (Objects.isNull(hospital.getProDocs()) || hospital.getProDocs().isEmpty() ||
                        hospital.getProDocs().stream().noneMatch(ProDoc::hasSpecialist)) {
                        return false;
                    }
                    break;
                case "현재운영중":
                    if (Objects.isNull(hospital.getHospitalDetail()) || 
                        !isCurrentlyOpen(hospital.getHospitalDetail())) {
                        return false;
                    }
                    break;
                case "무료주차":
                    if (Objects.isNull(hospital.getHospitalDetail()) || 
                        !hospital.getHospitalDetail().hasParkingSpace() ||  // ✅ 주차공간 먼저 체크
                        !hospital.getHospitalDetail().isFreeParking()) {     // ✅ 그 다음 무료 여부 체크
                        return false;
                    }
                    break;
                // ✅ 주말진료를 토요일/일요일로 분리
                case "토요일진료":
                    if (Objects.isNull(hospital.getHospitalDetail()) || 
                        !hospital.getHospitalDetail().isSaturdayAvailable()) {
                        return false;
                    }
                    break;
                case "일요일진료":
                    if (Objects.isNull(hospital.getHospitalDetail()) || 
                        !hospital.getHospitalDetail().isSundayAvailable()) {
                        return false;
                    }
                    break;
                // 기타 진료과목 태그 처리 (ex: "내과", "외과", "치과" 등)
                default:
                    if (Objects.isNull(hospital.getMedicalSubjects()) || hospital.getMedicalSubjects().isEmpty() ||
                        hospital.getMedicalSubjects().stream().noneMatch(ms -> ms.getSubjectName().equals(tag))) {
                        return false;
                    }
                    break;
            }
        }
        return true; // 모든 태그 조건을 만족하면 true
    }
    
    // ========== 운영시간 체크 로직 (평일 + 주말) ==========
    
    /**
     * 현재 시간에 운영중인지 체크 (평일 + 주말)
     */
    private static boolean isCurrentlyOpen(HospitalDetail detail) {
        LocalDateTime now = CurrentTimeUtils.getCurrentDateTime();
        DayOfWeek today = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();
        
        // 평일이면 평일 운영시간 체크
        if (CurrentTimeUtils.isCurrentlyWeekday()) {
            return isOpenAtTime(detail, today, currentTime);
        } else {
            // 주말이면 주말 운영시간 체크
            return isWeekendOpen(detail, today, currentTime);
        }
    }
    
    /**
     * 평일 운영시간 체크
     */
    private static boolean isOpenAtTime(HospitalDetail detail, DayOfWeek dayOfWeek, LocalTime time) {
        String openTime = null;
        String closeTime = null;
        
        switch (dayOfWeek) {
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
            default:
                return false;
        }
        
        return isTimeInOperatingRange(time, openTime, closeTime);
    }
    
    /**
     * 주말 운영시간 체크
     */
    private static boolean isWeekendOpen(HospitalDetail detail, DayOfWeek dayOfWeek, LocalTime time) {
        String openTime = null;
        String closeTime = null;
        
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            openTime = detail.getTrmtSatStart();
            closeTime = detail.getTrmtSatEnd();
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            openTime = detail.getTrmtSunStart();
            closeTime = detail.getTrmtSunEnd();
        }
        
        // 주말 진료시간이 설정되어 있으면 해당 시간으로 체크
        if (openTime != null && closeTime != null && 
            !openTime.trim().isEmpty() && !closeTime.trim().isEmpty() &&
            !"(NULL)".equals(openTime) && !"(NULL)".equals(closeTime) &&
            openTime.matches("\\d{4}") && closeTime.matches("\\d{4}")) {
            return isTimeInOperatingRange(time, openTime, closeTime);
        }
        
        // 주말 진료시간이 없으면 응급실 운영 여부로 판단
        return detail.hasEmergencyService();
    }
    
    /**
     * 운영시간 범위 체크 (4자리 숫자 형태)
     */
    private static boolean isTimeInOperatingRange(LocalTime currentTime, String openTime, String closeTime) {
        if (openTime == null || closeTime == null) {
            return false;
        }
        
        try {
            LocalTime open = parse4DigitTime(openTime);
            LocalTime close = parse4DigitTime(closeTime);
            
            if (open == null || close == null) {
                return false;
            }
            
            return !currentTime.isBefore(open) && !currentTime.isAfter(close);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 4자리 숫자 시간을 LocalTime으로 변환 (0900 → 09:00)
     */
    private static LocalTime parse4DigitTime(String timeStr) {
        if (timeStr == null || !timeStr.matches("\\d{4}")) {
            return null;
        }
        
        try {
            int hour = Integer.parseInt(timeStr.substring(0, 2));
            int minute = Integer.parseInt(timeStr.substring(2, 4));
            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            return null;
        }
    }
}
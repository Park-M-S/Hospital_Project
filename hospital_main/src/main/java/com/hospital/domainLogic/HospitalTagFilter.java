package com.hospital.domainLogic;

import com.hospital.entity.HospitalMain;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.ProDoc;
import com.hospital.util.CurrentTimeUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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
                        !hospital.getHospitalDetail().hasParkingSpace() ||
                        !hospital.getHospitalDetail().isFreeParking()) {
                        return false;
                    }
                    break;
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
                default:
                    if (Objects.isNull(hospital.getMedicalSubjects()) || hospital.getMedicalSubjects().isEmpty() ||
                        hospital.getMedicalSubjects().stream().noneMatch(ms -> ms.getSubjectName().equals(tag))) {
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
        
        if (CurrentTimeUtils.isCurrentlyWeekday()) {
            return isOpenAtTimeSimple(detail, today);
        } else {
            return isWeekendOpenSimple(detail, today);
        }
    }

    /**
     * 평일 운영시간 체크 
     */
    private static boolean isOpenAtTimeSimple(HospitalDetail detail, DayOfWeek dayOfWeek) {
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
        
     
        return isValidTime(openTime) && isValidTime(closeTime);
    }

    /**
     * 주말 운영시간 체크 
     */
    private static boolean isWeekendOpenSimple(HospitalDetail detail, DayOfWeek dayOfWeek) {
        // HospitalDetail의 기존 메서드들을 활용
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return detail.isSaturdayAvailable();
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            return detail.isSundayAvailable();
        }
        
        // 주말 진료시간이 없으면 응급실 운영 여부로 판단
        return detail.hasEmergencyService();
    }

    /**
     * 유효한 시간 값인지 체크 
     */
    private static boolean isValidTime(String timeStr) {
        if (timeStr == null || 
            timeStr.trim().isEmpty() || 
            "(NULL)".equals(timeStr) || 
            !timeStr.matches("\\d{4}")) {
            return false;
        }

        if ("0000".equals(timeStr)) {
            return false;
        }

        return true;
    }
}
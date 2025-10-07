package com.hospital.entity;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "hospital_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@ToString(exclude = "hospital")
public class HospitalDetail {

    @Id
    @Column(name = "hospital_code", length = 255, nullable = false)
    private String hospitalCode;

    @Column(name = "em_day_yn", length = 1)
    private String emyDayYn;//필요없음

    @Column(name = "em_night_yn", length = 1)
    private String emyNightYn;//필요없음

    @Column(name = "parking_capacity")
    private Integer parkQty;

    @Column(name = "park_xpns_yn")
    private String parkXpnsYn;

    @Column(name = "weekday_lunch", length = 50)
    private String lunchWeek;

    @Column(name = "weekday_reception", length = 50)
    private String rcvWeek;//필요없음

    @Column(name = "saturday_reception", length = 50)
    private String rcvSat;//필요없음

    @Column(name = "noTrmtHoli")
    private String noTrmtHoli;

    @Column(name = "noTrmtSun")
    private String noTrmtSun;

    // 요일별 진료 시작/종료 시간
    @Column(name = "mon_open", length = 20)
    private String trmtMonStart;
    @Column(name = "mon_end", length = 20)
    private String trmtMonEnd;

    @Column(name = "tues_open", length = 20)
    private String trmtTueStart;
    @Column(name = "tues_end", length = 20)
    private String trmtTueEnd;

    @Column(name = "wed_open", length = 20)
    private String trmtWedStart;
    @Column(name = "wed_end", length = 20)
    private String trmtWedEnd;

    @Column(name = "thurs_open", length = 20)
    private String trmtThurStart;
    @Column(name = "thurs_end", length = 20)
    private String trmtThurEnd;

    @Column(name = "fri_open", length = 20)
    private String trmtFriStart;
    @Column(name = "fri_end", length = 20)
    private String trmtFriEnd;

    @Column(name = "trmt_sat_start")
    private String trmtSatStart;

    @Column(name = "trmt_sat_end")
    private String trmtSatEnd;

    @Column(name = "trmt_sun_start")
    private String trmtSunStart;

    @Column(name = "trmt_sun_end")
    private String trmtSunEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_code", insertable = false, updatable = false)
    private HospitalMain hospital;

    //주차 가능 여부 체크
    public boolean hasParkingSpace() {
        return this.parkQty != null && this.parkQty > 0;
    }

    //무료주차 가능 여부
    public boolean isFreeParking() {
        return "N".equals(this.parkXpnsYn);
    }

    
    //토요일 진료 가능 여부 
    public boolean isSaturdayAvailable() {
        return isValidTime(this.trmtSatStart) && isValidTime(this.trmtSatEnd);
    }

    //일요일 진료 가능 여부
    public boolean isSundayAvailable() {
        return isValidTime(this.trmtSunStart) && isValidTime(this.trmtSunEnd);
    }

    
    //유효한 시간 값인지 체크
    public static boolean isValidTime(String timeStr) {
        if (timeStr == null) {
        	return false;
        }
        if(
            timeStr.trim().isEmpty() || 
            "(NULL)".equals(timeStr) || 
            !timeStr.matches("\\d{4}")) {
            return false;
        }

        // 0000은 운영하지 않는 시간으로 처리
        if ("0000".equals(timeStr)) {
            return false;
        }

        return true;
    }
}
package com.hospital.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "hospital_detail") // 데이터베이스 테이블 이름
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Builder 패턴 사용을 위해 추가 (옵션)
@ToString // toString() 메서드 자동 생성
public class HospitalDetail {

    @Id
    @Column(name = "hospital_code", length = 255, nullable = false) // 병원 고유 코드 (ykiho)
    private String hospitalCode; // API의 <ykiho>

    @Column(name = "em_day_yn", length = 1)
    private String emyDayYn; // 주간 응급 진료 가능 여부 (Y/N)

    @Column(name = "em_night_yn", length = 1)
    private String emyNightYn; // 야간 응급 진료 가능 여부 (Y/N)

    @Column(name = "parking_capacity")
    private Integer parkQty; // 주차 가능 대수 (API는 parkQty)
    
    // 주차비 유료 여부
    @Column(name = "park_xpns_yn")
    private String parkXpnsYn;

    @Column(name = "weekday_lunch", length = 50)
    private String lunchWeek; // 점심시간

    @Column(name = "weekday_reception", length = 50)
    private String rcvWeek; // 평일 접수 시간

    @Column(name = "saturday_reception", length = 50)
    private String rcvSat; // 토요일 접수 시간

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
    
 // 토요일 진료시간
    @Column(name = "trmt_sat_start")
    private String trmtSatStart;

    @Column(name = "trmt_sat_end") 
    private String trmtSatEnd;

    // 일요일 진료시간
    @Column(name = "trmt_sun_start")
    private String trmtSunStart;

    @Column(name = "trmt_sun_end")
    private String trmtSunEnd;

  
    
    @OneToOne
    @JoinColumn(name = "hospital_code", referencedColumnName = "hospital_code", insertable = false, updatable = false)
    private HospitalMain hospital;
    
    
   
    
    // ✅ 응급실 서비스 가능 여부 체크
    public boolean hasEmergencyService() {
        // em_day_yn과 em_night_yn이 둘 다 null이거나 둘 다 'N'이면 false
        boolean dayAvailable = "Y".equals(this.emyDayYn);
        boolean nightAvailable = "Y".equals(this.emyNightYn);
        
        return dayAvailable || nightAvailable; // 하나라도 Y면 true
    }
    
    // ✅ 주차 가능 여부 체크
    public boolean hasParkingSpace() {
        // parking_capacity가 null이거나 0이면 false
        return this.parkQty != null && this.parkQty > 0;
    }
    /**
     * 무료주차 가능 여부
     */
    public boolean isFreeParking() {
        return "N".equals(this.parkXpnsYn); // N = 무료
    }

    /**
     * 토요일 진료 가능 여부
     */
    public boolean isSaturdayAvailable() {
        return isValidTime(this.trmtSatStart) && isValidTime(this.trmtSatEnd);
    }

    /**
     * 일요일 진료 가능 여부
     */
    public boolean isSundayAvailable() {
        return isValidTime(this.trmtSunStart) && isValidTime(this.trmtSunEnd);
    }

    /**
     * 주말 진료 가능 여부 (토요일 또는 일요일)
     */
    public boolean isWeekendAvailable() {
        return isSaturdayAvailable() || isSundayAvailable() || hasEmergencyService();
    }

    /**
     * 유효한 시간 값인지 체크
     */
    private boolean isValidTime(String timeStr) {
        return timeStr != null && 
               !timeStr.trim().isEmpty() && 
               !"(NULL)".equals(timeStr) &&
               timeStr.matches("\\d{4}"); // 4자리 숫자
    }
  
    
}

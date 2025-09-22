package com.hospital.entity;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@ToString(exclude = "hospital")
@Table(name = "pro_doc" )
public class ProDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hospital_code", nullable = false)
    private String hospitalCode; //실제 DB에 저장될 병원 코드 (외래키)
    
    
    @Column(name = "med_general_cnt")
    private String medGeneralCnt;  // mdeptGdrCnt
    
    @Column(name = "med_intern_cnt")
    private String medInternCnt;  // mdeptIntnCnt
    
    @Column(name = "med_resident_cnt")
    private String medResidentCnt;  // mdeptResdntCnt
    
    @Column(name = "med_specialist_cnt")
    private String medSpecialistCnt;  // mdeptSdrCnt
    
    // 치과
    @Column(name = "dent_general_cnt")
    private String dentGeneralCnt;  // detyGdrCnt
    
    @Column(name = "dent_intern_cnt")
    private String dentInternCnt;  // detyIntnCnt
    
    @Column(name = "dent_resident_cnt")
    private String dentResidentCnt;  // detyResdntCnt
    
    @Column(name = "dent_specialist_cnt")
    private String dentSpecialistCnt;  // detySdrCnt
    
    // 한방
    @Column(name = "oriental_general_cnt")
    private String orientalGeneralCnt;  // cmdcGdrCnt
    
    @Column(name = "oriental_intern_cnt")
    private String orientalInternCnt;  // cmdcIntnCnt
    
    @Column(name = "oriental_resident_cnt")
    private String orientalResidentCnt;  // cmdcResdntCnt
    
    @Column(name = "oriental_specialist_cnt")
    private String orientalSpecialistCnt;  // cmdcSdrCnt
    
    // 기타
    @Column(name = "midwife_cnt")
    private String midwifeCnt;  // pnursCnt
    
    
    

    //N:1 병원 관계 매핑 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_code", referencedColumnName = "hospital_code", insertable = false, updatable = false)
    private HospitalMain hospital;
    
    
    //public boolean hasSpecialist() {
        // proDocCount가 null이거나 0이면 false
        //return this.proDocCount != null && this.proDocCount > 0;
//}
    
    

    //전문의 존재 여부 체크
    //public boolean hasSpecialist() {
        //return this.proDocList != null && !this.proDocList.trim().isEmpty();
   //}
    
 
}

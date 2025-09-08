package com.hospital.entity;

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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "medical_subject")
public class MedicalSubject {

    // 고유 ID (PK, AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 병원 코드 (외래키 역할 수행)
    @Column(name = "hospital_code", nullable = false)
    private String hospitalCode;

    // 진료과목명 (ex. 내과, 치과, 한의과 등)
    @Column(name = "subject_name")
    private String subjectName;

   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "hospital_code",                 // 이 테이블의 외래키 컬럼
        referencedColumnName = "hospital_code", // 병원 테이블의 PK 컬럼
        insertable = false, 
        updatable = false
    )
    private HospitalMain hospital;

   
}

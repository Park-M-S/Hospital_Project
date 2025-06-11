package com.hospital.entity;

import jakarta.persistence.*;

/**
 * ✅ 진료과목 정보를 저장하는 Entity 클래스
 * - DB 테이블명: medical_subject
 * - 병원 코드(hospitalCode)를 외래키로 사용
 * - 병원(Hospital)과 다대일 관계 (N:1)로 연결됨
 */
@Entity
@Table(name = "medical_subject")
public class MedicalSubject {

    // ✅ 고유 ID (PK, AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 병원 코드 (외래키 역할 수행)
    @Column(name = "hospital_code", nullable = false)
    private String hospitalCode;

    // ✅ 진료과목명 (ex. 내과, 치과, 한의과 등)
    @Column(name = "subject_name")
    private String subjectName;

    /**
     * ✅ 병원과의 연관관계 매핑 (N:1)
     * - 이 필드는 외래키 조인용 참조 객체이며, 실제 저장은 hospitalCode 필드에서 처리함
     * - insertable=false, updatable=false 로 직접 수정은 제한
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "hospital_code",                 // 이 테이블의 외래키 컬럼
        referencedColumnName = "hospital_code", // 병원 테이블의 PK 컬럼
        insertable = false, 
        updatable = false
    )
    private HospitalMain hospital;

    // ✅ 기본 생성자 (JPA 필수)
    public MedicalSubject() {}

    // ✅ Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public HospitalMain getHospital() { return hospital; }
    public void setHospital(HospitalMain hospital) { this.hospital = hospital; }
}

package com.hospital.entity;

import java.util.List;

import jakarta.persistence.*;


@Entity
@Table(name = "pro_doc")
public class ProDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hospital_code", nullable = false)
    private String hospitalCode; // ✅ 실제 DB에 저장될 병원 코드 (외래키)

    @Column(name = "subject_name")
    private String subjectName;

    @Column(name = "pro_doc_count")
    private Integer proDocCount;

    // ✅ N:1 병원 관계 매핑 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_code", referencedColumnName = "hospital_code", insertable = false, updatable = false)
    private HospitalMain hospital;
    
    

    // ✅ 전문의 존재 여부 체크
    public boolean hasSpecialist() {
        // proDocCount가 null이거나 0이면 false
        return this.proDocCount != null && this.proDocCount > 0;
    }

    // 기본 생성자
    public ProDoc() {}

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHospitalCode() { return hospitalCode; }
    public void setHospitalCode(String hospitalCode) { this.hospitalCode = hospitalCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getProDocCount() { return proDocCount; }
    public void setProDocCount(Integer proDocCount) { this.proDocCount = proDocCount; }

    public HospitalMain getHospital() { return hospital; }
    public void setHospital(HospitalMain hospital) { this.hospital = hospital; }
}

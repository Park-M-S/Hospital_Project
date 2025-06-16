package com.hospital.entity;

import java.util.Set;

import jakarta.persistence.CascadeType;
// JPA 관련 임포트 추가
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
// import jakarta.persistence.GeneratedValue; // 필요한 경우 (자동 생성되는 ID)
// import jakarta.persistence.GenerationType; // 필요한 경우
// Lombok 어노테이션은 유지합니다.
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString; // toString() 메서드를 자동으로 생성해주는 어노테이션도 추가하면 디버깅에 유용합니다.

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString // 디버깅에 유용
@Entity
@Table(name = "hospital_main")
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "hospital-with-detail",
        attributeNodes = @NamedAttributeNode("hospitalDetail")
    ),
    @NamedEntityGraph(
        name = "hospital-with-medical-subjects", 
        attributeNodes = @NamedAttributeNode("medicalSubjects")
    ),
    @NamedEntityGraph(
        name = "hospital-with-pro-docs",
        attributeNodes = @NamedAttributeNode("proDocs")
    ),
    // ✅ 새로 추가 - 모든 연관관계를 한번에 로딩
    @NamedEntityGraph(
        name = "hospital-with-all",
        attributeNodes = {
            @NamedAttributeNode("hospitalDetail"),
            @NamedAttributeNode("medicalSubjects"), 
            @NamedAttributeNode("proDocs")
        }
    )
})
public class HospitalMain {

    @Id // 이 필드가 테이블의 기본 키(Primary Key)임을 나타냅니다.
    @Column(name = "hospital_code", nullable = false, length = 255) // 컬럼 이름과 속성 지정
    private String hospitalCode; // PK

    @Column(name = "hospital_name", nullable = false, length = 255)
    private String hospitalName;

    @Column(name = "province_name", length = 100)
    private String provinceName;

    @Column(name = "district_name", length = 100)
    private String districtName;

    @Column(name = "hospital_address", length = 500)
    private String hospitalAddress;

    @Column(name = "hospital_tel", length = 20)
    private String hospitalTel;

    @Column(name = "hospital_homepage", length = 255)
    private String hospitalHomepage;

    @Column(name = "doctor_num") // int 타입은 기본적으로 DB에서 INTEGER로 매핑됩니다.
    private int doctorNum;

    @Column(name = "coordinate_x") // double 타입은 기본적으로 DB에서 DOUBLE 또는 REAL로 매핑됩니다.
    private Double coordinateX;

    @Column(name = "coordinate_y")
    private Double coordinateY;
    
    @OneToOne(mappedBy = "hospital", 
            fetch = FetchType.LAZY, 
            cascade = CascadeType.ALL, 
            orphanRemoval = true)
  private HospitalDetail hospitalDetail;


  @OneToMany(mappedBy = "hospital", 
             fetch = FetchType.LAZY, 
             cascade = CascadeType.ALL, 
             orphanRemoval = true)
  private Set<MedicalSubject> medicalSubjects;


  @OneToMany(mappedBy = "hospital", 
             fetch = FetchType.LAZY, 
             cascade = CascadeType.ALL, 
             orphanRemoval = true)
  private Set<ProDoc> proDocs;
}
    


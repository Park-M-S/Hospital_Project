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
    
    @Column(name = "subject_name")
    private String subjectName;
    
    
    @Column(name = "pro_doc_count")
    private Integer proDocCount;
    
    

    //N:1 병원 관계 매핑 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_code", referencedColumnName = "hospital_code", insertable = false, updatable = false)
    private HospitalMain hospital;
    
    //public boolean hasSpecialist() {
        // proDocCount가 null이거나 0이면 false
        //return this.proDocCount != null && this.proDocCount > 0;
//}
    
    

    public boolean hasSpecialist() {
        return this.proDocCount != null && this.proDocCount > 0;  // Integer 비교
    }
    
 
}
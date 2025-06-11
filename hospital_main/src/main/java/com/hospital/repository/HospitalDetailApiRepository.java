package com.hospital.repository;

import com.hospital.entity.HospitalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface HospitalDetailApiRepository extends JpaRepository<HospitalDetail, String> {
    // JpaRepository는 기본적인 CRUD (Create, Read, Update, Delete) 메서드를 제공합니다.
    // HospitalDetail 엔티티와 그 엔티티의 ID 타입(String)을 지정합니다.

    // 필요한 경우 여기에 커스텀 쿼리 메서드를 추가할 수 있습니다.
    // 예: findByEmDayYn(String emDayYn);
}
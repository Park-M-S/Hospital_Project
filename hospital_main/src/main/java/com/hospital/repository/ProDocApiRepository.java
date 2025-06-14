package com.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.entity.ProDoc;

public interface ProDocApiRepository extends JpaRepository<ProDoc, Long> {
	

    /**

     * ✅ 테이블 데이터 전체 삭제 (외래키 제약에도 안전)

     */

    @Modifying

    @Transactional

    @Query(value = "DELETE FROM pro_doc", nativeQuery = true)

    void deleteAllProDocs();



    /**

     * ✅ AUTO_INCREMENT 값을 1로 초기화

     */

    @Modifying

    @Transactional

    @Query(value = "ALTER TABLE pro_doc AUTO_INCREMENT = 1", nativeQuery = true)

    void resetAutoIncrement();
}

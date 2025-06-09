package com.hospital.repository;

import java.util.List;

import com.hospital.entity.HospitalEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HospitalRepositoryImpl implements HospitalRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HospitalRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<HospitalEntity> getAllHospitals(String sub) {
        String sql = "SELECT hm.hospital_code, hm.hospital_name, hms.subject_name, hm.hospital_address, " +
                     "hm.coordinate_x, hm.coordinate_y, hm.emergency_available, hm.park_available, hm.pro_doc " +
                     "FROM hospital_main hm " +
                     "JOIN hospital_medical_subject hms ON hm.hospital_code = hms.hospital_code " +
                     "WHERE hms.subject_name LIKE CONCAT('%', ?, '%')";

        return jdbcTemplate.query(sql, ps -> ps.setString(1, sub), new HospitalEntityRowMapper());
    }
}
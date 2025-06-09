package com.hospital.dao;

import java.util.List;

import com.hospital.model.Hospital;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HospitalDAOImpl implements HospitalDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HospitalDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Hospital> getAllHospitals() {
        String sql = "SELECT hospital_name, hospital_address, hospital_tel, doctor_num, coordinate_x, coordinate_y FROM hospital_main";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String name = rs.getString("hospital_name");
            String address = rs.getString("hospital_address");
            String tel = rs.getString("hospital_tel");
            int doc = rs.getInt("doctor_num");
            double x = rs.getDouble("coordinate_x");
            double y = rs.getDouble("coordinate_y");
            return new Hospital(name, address, tel, doc, x, y);
        });
    }
}
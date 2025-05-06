package com.hospital.dao;

import java.util.List;

import com.hospital.dto.HospitalDTO;
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
    public List<HospitalDTO> getAllHospitals(String sub) {
        String sql = "SELECT hospital_name, medical_subject, hospital_address, coordinate_x, coordinate_y from hospital_main where medical_subject LIKE CONCAT('%', ?, '%')";

        return jdbcTemplate.query(sql, ps -> ps.setString(1, sub), (rs, rowNum) -> {
        	String name = rs.getString("hospital_name");
            String subject = rs.getString("medical_subject");
            String address = rs.getString("hospital_address");
            double x = rs.getDouble("coordinate_x");
            double y = rs.getDouble("coordinate_y");
            return new HospitalDTO(name, subject, address, x, y);
        });
    }
}
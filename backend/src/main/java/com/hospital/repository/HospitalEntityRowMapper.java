package com.hospital.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.hospital.entity.HospitalEntity;

public class HospitalEntityRowMapper implements RowMapper<HospitalEntity> {

    @Override
    public HospitalEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        HospitalEntity hospitalEntity = new HospitalEntity();
        hospitalEntity.setHospitalCode(rs.getString("hospital_code"));
        hospitalEntity.setHospitalSubject(rs.getString("subject_name"));
        hospitalEntity.setHospitalName(rs.getString("hospital_name"));
        hospitalEntity.setHospitalAddress(rs.getString("hospital_address"));
        hospitalEntity.setCoordinateX(rs.getDouble("coordinate_x"));
        hospitalEntity.setCoordinateY(rs.getDouble("coordinate_y"));
        hospitalEntity.setEmergencyAvailable(rs.getString("emergency_available"));
        hospitalEntity.setParkAvailable(rs.getInt("park_available"));
        hospitalEntity.setProDoc(rs.getInt("pro_doc"));
        return hospitalEntity;
    }
}
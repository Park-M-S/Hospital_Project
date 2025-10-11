package com.hospital.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.hospital.dto.HospitalWebResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HospitalJdbcRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public List<HospitalWebResponse> findByMBRDirect(
            double minLon, double maxLon, 
            double minLat, double maxLat) {
        
        // 1. Hospital + Detail 조회
        List<HospitalWebResponse> hospitals = queryHospitals(minLon, maxLon, minLat, maxLat);
        
        if (hospitals.isEmpty()) {
            return hospitals;
        }
        
        // 2. 연관 데이터 로드
        loadMedicalSubjects(hospitals);
        loadProDocs(hospitals);
        
        return hospitals;
    }
    
    private List<HospitalWebResponse> queryHospitals(double minLon, double maxLon, double minLat, double maxLat) {
        String sql = """
            SELECT
                h.hospital_code, h.hospital_name, h.hospital_address, h.hospital_tel,
                h.doctor_num, h.coordinate_x, h.coordinate_y,
                d.weekday_lunch, d.parking_capacity, d.park_xpns_yn, d.noTrmtHoli, d.noTrmtSun,
                d.mon_open, d.mon_end, d.tues_open, d.tues_end,
                d.wed_open, d.wed_end, d.thurs_open, d.thurs_end,
                d.fri_open, d.fri_end, d.trmt_sat_start, d.trmt_sat_end,
                d.trmt_sun_start, d.trmt_sun_end
            FROM hospital_main h
            LEFT JOIN hospital_detail d ON h.hospital_code = d.hospital_code
            WHERE MBRContains(
                ST_GeomFromText(
                    CONCAT('POLYGON((', ?, ' ', ?, ',', ?, ' ', ?, ',',
                                        ?, ' ', ?, ',', ?, ' ', ?, ',', ?, ' ', ?, '))'),
                    4326
                ),
                h.location
            )
            """;

        return jdbcTemplate.query(sql,
            new HospitalRowMapper(),
            minLon, minLat, maxLon, minLat, maxLon, maxLat, minLon, maxLat, minLon, minLat
        );
    }
    private void loadMedicalSubjects(List<HospitalWebResponse> hospitals) {
        List<String> codes = hospitals.stream().map(HospitalWebResponse::getHospitalCode).toList();
        String placeholders = String.join(",", Collections.nCopies(codes.size(), "?"));
        
        Map<String, List<String>> map = new HashMap<>();
        jdbcTemplate.query(
            "SELECT hospital_code, subjects FROM medical_subject WHERE hospital_code IN (" + placeholders + ")",
            rs -> {
                String code = rs.getString(1);
                String subjects = rs.getString(2);
                if (subjects != null) {
                    map.computeIfAbsent(code, k -> new ArrayList<>())
                       .addAll(Arrays.asList(subjects.split(",")));
                }
            },
            codes.toArray()
        );
        
        hospitals.forEach(h -> h.setMedicalSubjects(map.getOrDefault(h.getHospitalCode(), List.of())));
    }
    
    private void loadProDocs(List<HospitalWebResponse> hospitals) {
        List<String> codes = hospitals.stream().map(HospitalWebResponse::getHospitalCode).toList();
        String placeholders = String.join(",", Collections.nCopies(codes.size(), "?"));
        
        Map<String, Map<String, Integer>> map = new HashMap<>();
        jdbcTemplate.query(
            "SELECT hospital_code, subject_name, pro_doc_count FROM pro_doc WHERE hospital_code IN (" + placeholders + ")",
            rs -> {
                String code = rs.getString(1);
                String subject = rs.getString(2);
                Integer count = rs.getInt(3);
                map.computeIfAbsent(code, k -> new HashMap<>()).merge(subject, count, Integer::sum);
            },
            codes.toArray()
        );
        
        hospitals.forEach(h -> h.setProfessionalDoctors(map.getOrDefault(h.getHospitalCode(), Map.of())));
    }
}
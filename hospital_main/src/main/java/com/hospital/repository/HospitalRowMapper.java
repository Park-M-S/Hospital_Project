package com.hospital.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.springframework.jdbc.core.RowMapper;

import com.hospital.dto.HospitalWebResponse;

public class HospitalRowMapper implements RowMapper<HospitalWebResponse> {

	@Override
	public HospitalWebResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
		return HospitalWebResponse.builder().hospitalCode(rs.getString("hospital_code"))
				.hospitalName(rs.getString("hospital_name")).hospitalAddress(rs.getString("hospital_address"))
				.hospitalTel(rs.getString("hospital_tel")).totalDoctors(parseInteger(rs.getString("doctor_num")))
				.coordinateX(rs.getDouble("coordinate_x")).coordinateY(rs.getDouble("coordinate_y"))

				// hospital_detail 테이블
				.weekdayLunch(rs.getString("weekday_lunch")).parkingCapacity(rs.getInt("parking_capacity"))
				.parkingFee("Y".equalsIgnoreCase(rs.getString("park_xpns_yn"))).noTrmtHoli(rs.getString("noTrmtHoli"))
				.noTrmtSun(rs.getString("noTrmtSun"))

				// 오늘 운영시간 (임시로 월요일 사용)
				.todayOpen(formatTime(rs.getString("mon_open"))).todayClose(formatTime(rs.getString("mon_end")))

				// 주간 스케줄
				.weeklySchedule(createWeeklySchedule(rs)).medicalSubjects(new ArrayList<>())
				.professionalDoctors(new HashMap<>()).build();
	}

	private Map<String, Map<String, String>> createWeeklySchedule(ResultSet rs) throws SQLException {
		Map<String, Map<String, String>> schedule = new LinkedHashMap<>();
		schedule.put("월요일", daySchedule(rs, "mon_open", "mon_end"));
		schedule.put("화요일", daySchedule(rs, "tues_open", "tues_end"));
		schedule.put("수요일", daySchedule(rs, "wed_open", "wed_end"));
		schedule.put("목요일", daySchedule(rs, "thurs_open", "thurs_end"));
		schedule.put("금요일", daySchedule(rs, "fri_open", "fri_end"));
		schedule.put("토요일", daySchedule(rs, "trmt_sat_start", "trmt_sat_end"));
		schedule.put("일요일", daySchedule(rs, "trmt_sun_start", "trmt_sun_end"));
		return schedule;
	}

	private Map<String, String> daySchedule(ResultSet rs, String start, String end) throws SQLException {
		Map<String, String> day = new LinkedHashMap<>();
		day.put("open", formatTime(rs.getString(start)));
		day.put("close", formatTime(rs.getString(end)));
		return day;
	}

	private String formatTime(String time) {
		if (time != null && time.length() == 4 && time.matches("\\d{4}")) {
			return time.substring(0, 2) + ":" + time.substring(2, 4);
		}
		return "";
	}

	private Integer parseInteger(String value) {
		try {
			return value != null && !value.trim().isEmpty() ? Integer.parseInt(value.trim()) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
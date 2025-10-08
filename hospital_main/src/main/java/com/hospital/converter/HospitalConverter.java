package com.hospital.converter;

import com.hospital.entity.HospitalMain;
import com.hospital.entity.MedicalSubject;
import com.hospital.dto.HospitalWebResponse;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.ProDoc;
import com.hospital.util.TodayOperatingTimeCalculator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HospitalConverter {

	// Hospital 엔티티를 HospitalResponseDto로 변환
	public HospitalWebResponse convertToDTO(HospitalMain hospitalMain) {
		if (hospitalMain == null) {
			return null;
		}

		HospitalDetail detail = hospitalMain.getHospitalDetail();

		TodayOperatingTimeCalculator.TodayOperatingTime todayTime = TodayOperatingTimeCalculator
				.getTodayOperatingTime(detail);

		return HospitalWebResponse.builder()
				// 기본 정보
				.hospitalCode(hospitalMain.getHospitalCode()).hospitalName(hospitalMain.getHospitalName())
				.hospitalAddress(hospitalMain.getHospitalAddress()).hospitalTel(hospitalMain.getHospitalTel())

				.totalDoctors(convertStringToInteger(hospitalMain.getTotalDoctors()))

				// 좌표 정보
				.coordinateX(hospitalMain.getCoordinateX()).coordinateY(hospitalMain.getCoordinateY())

				// 운영 정보 (detail이 있을 때만)

				.weekdayLunch(detail != null ? detail.getLunchWeek() : null)
				.parkingCapacity(detail != null ? detail.getParkQty() : null)
				.parkingFee(detail != null ? convertYnToBoolean(detail.getParkXpnsYn()) : null)
				.noTrmtHoli(detail != null ? detail.getNoTrmtHoli() : null)
				.noTrmtSun(detail != null ? detail.getNoTrmtSun() : null)

				// 운영 시간
				.todayOpen(formatTime(todayTime.getOpenTime())).todayClose(formatTime(todayTime.getCloseTime()))

				.weeklySchedule(convertToWeeklySchedule(detail))

				.medicalSubjects(convertMedicalSubjectsToList(hospitalMain.getMedicalSubjects()))

				// 전문의 정보를 문자열로 변환
				.professionalDoctors(convertProDocsToMap(hospitalMain.getProDocs())).build();
	}

	private Integer convertStringToInteger(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null; // 또는 0
		}
	}

	private List<String> convertMedicalSubjectsToList(Set<MedicalSubject> subjects) {
		if (subjects == null || subjects.isEmpty()) {
			return List.of();
		}

		return subjects.stream().map(MedicalSubject::getSubjects) // 엔티티의 subjects(String) 가져오기
				.filter(s -> s != null && !s.isBlank()) // null/빈 문자열 제거
				.flatMap(s -> Arrays.stream(s.split(","))) // 쉼표로 나누어 각 과목 분리
				.map(String::trim).filter(s -> !s.isEmpty()).distinct().sorted().collect(Collectors.toList());
	}

	// Hospital 엔티티 리스트를 DTO 리스트로 변환
	public List<HospitalWebResponse> convertToDtos(List<HospitalMain> hospitals) {
		if (hospitals == null) {
			return List.of();
		}

		return hospitals.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	// ProDoc의 개별 필드를 Map으로 변환
	private Map<String, Integer> convertProDocsToMap(Set<ProDoc> set) {
		if (set == null || set.isEmpty()) {
			return new HashMap<>();
		}

		return set.stream().filter(proDoc -> proDoc.getSubjectName() != null && proDoc.getProDocCount() != null)
				.collect(Collectors.toMap(ProDoc::getSubjectName, // 과목명을 키로
						ProDoc::getProDocCount, // 이미 Integer이므로 바로 사용
						Integer::sum // 중복 시 합산
				));
	}

	private Boolean convertYnToBoolean(String ynValue) {
		if (ynValue == null) {
			return null;
		}
		return "Y".equalsIgnoreCase(ynValue.trim());
	}

	// detail == null,detail != null, 시간 모두 null이여도 같은 구조 반환 (나중에 디테일 구조 바뀌면 꼭!!
	// 수정할것)
	private Map<String, Map<String, String>> convertToWeeklySchedule(HospitalDetail detail) {
		Map<String, Map<String, String>> schedule = new LinkedHashMap<>();

		schedule.put("월요일", createDaySchedule(detail != null ? detail.getTrmtMonStart() : null,
				detail != null ? detail.getTrmtMonEnd() : null));
		schedule.put("화요일", createDaySchedule(detail != null ? detail.getTrmtTueStart() : null,
				detail != null ? detail.getTrmtTueEnd() : null));
		schedule.put("수요일", createDaySchedule(detail != null ? detail.getTrmtWedStart() : null,
				detail != null ? detail.getTrmtWedEnd() : null));
		schedule.put("목요일", createDaySchedule(detail != null ? detail.getTrmtThurStart() : null,
				detail != null ? detail.getTrmtThurEnd() : null));
		schedule.put("금요일", createDaySchedule(detail != null ? detail.getTrmtFriStart() : null,
				detail != null ? detail.getTrmtFriEnd() : null));
		schedule.put("토요일", createDaySchedule(detail != null ? detail.getTrmtSatStart() : null,
				detail != null ? detail.getTrmtSatEnd() : null));
		schedule.put("일요일", createDaySchedule(detail != null ? detail.getTrmtSunStart() : null,
				detail != null ? detail.getTrmtSunEnd() : null));

		return schedule;
	}

	private Map<String, String> createDaySchedule(String startTime, String endTime) {
		Map<String, String> daySchedule = new LinkedHashMap<>();
		daySchedule.put("open", formatTime(startTime));
		daySchedule.put("close", formatTime(endTime));
		return daySchedule;
	}

	private String formatTime(String timeStr) {
		if (HospitalDetail.isValidTime(timeStr)) {
			return timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4);
		}
		return "";

	}

}
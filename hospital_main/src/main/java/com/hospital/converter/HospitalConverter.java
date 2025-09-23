package com.hospital.converter;

import com.hospital.entity.HospitalMain;
import com.hospital.entity.MedicalSubject;
import com.hospital.dto.HospitalWebResponse;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.ProDoc;
import com.hospital.util.TodayOperatingTimeCalculator;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
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
				.hospitalName(hospitalMain.getHospitalName()).hospitalAddress(hospitalMain.getHospitalAddress())
				.provinceName(hospitalMain.getProvinceName()).districtName(hospitalMain.getDistrictName())
				.hospitalTel(hospitalMain.getHospitalTel()).hospitalHomepage(hospitalMain.getHospitalHomepage())
				.totalDoctors(convertStringToInteger(hospitalMain.getTotalDoctors()))

				// 좌표 정보
				.coordinateX(hospitalMain.getCoordinateX()).coordinateY(hospitalMain.getCoordinateY())

				// 운영 정보 (detail이 있을 때만)
				.emergencyDayAvailable(detail != null ? convertYnToBoolean(detail.getEmyDayYn()) : null)
				.emergencyNightAvailable(detail != null ? convertYnToBoolean(detail.getEmyNightYn()) : null)
				.weekdayLunch(detail != null ? detail.getLunchWeek() : null)
				.parkingCapacity(detail != null ? detail.getParkQty() : null)
				.parkingFee(detail != null ? convertYnToBoolean(detail.getParkXpnsYn()) : null)

				// 운영 시간
				.todayOpen(formatTime(todayTime.getOpenTime())).todayClose(formatTime(todayTime.getCloseTime()))

				.medicalSubjects(convertMedicalSubjectsToList(hospitalMain.getMedicalSubjects()))

				// 전문의 정보를 문자열로 변환
				.professionalDoctors(convertProDocsToMap(hospitalMain.getProDocs()))
				.build();
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

	private Integer convertStringToInteger(String value) {
	    if (value == null || value.trim().isEmpty()) {
	        return null;
	    }
	    try {
	        return Integer.parseInt(value.trim());
	    } catch (NumberFormatException e) {
	        return null;  // 또는 0
	    }
	}

	private String formatTime(String timeStr) {
		// null이거나 4자리가 아니면 원본값 그대로 반환
		if (timeStr == null || timeStr.length() != 4) {
			return timeStr;
		}

		// HHMM을 HH:MM으로 변환
		return timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4);
	}

	// Hospital 엔티티 리스트를 DTO 리스트로 변환
	public List<HospitalWebResponse> convertToDtos(List<HospitalMain> hospitals) {
		if (hospitals == null) {
			return List.of();
		}

		return hospitals.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	
	private Map<String, Integer> convertProDocsToMap(Set<ProDoc> set) {
	    System.out.println("=== ProDoc 디버깅 시작 ===");
	    System.out.println("ProDoc set is null: " + (set == null));
	    System.out.println("ProDoc set size: " + (set != null ? set.size() : 0));
	    
	    if (set == null || set.isEmpty()) {
	        System.out.println("ProDoc set이 비어있음");
	        return new HashMap<>();
	    }

	    // 각 ProDoc 확인
	    for (ProDoc proDoc : set) {
	        System.out.println("ProDoc ID: " + proDoc.getId());
	        System.out.println("ProDoc List: " + proDoc.getProDocList());
	    }

	    Map<String, Integer> result = set.stream()
	            .filter(proDoc -> proDoc.getProDocList() != null)
	            .flatMap(proDoc -> convertStringToSubjectMap(proDoc.getProDocList()).entrySet().stream())
	            .collect(Collectors.toMap(
	                Map.Entry::getKey,
	                Map.Entry::getValue,
	                Integer::sum
	            ));
	    
	    System.out.println("최종 결과: " + result);
	    System.out.println("=== ProDoc 디버깅 끝 ===");
	    return result;
	}
	
	// 문자열 파싱 메서드: "내과(5명), 외과(3명)" → Map<String, Integer>
	private Map<String, Integer> convertStringToSubjectMap(String proDocList) {
	    Map<String, Integer> result = new HashMap<>();
	    if (proDocList == null || proDocList.trim().isEmpty()) {
	        return result;
	    }

	    String[] subjects = proDocList.split(", ");
	    for (String subject : subjects) {
	        // "가정의학과(1)" → "가정의학과"=1
	        if (subject.contains("(") && subject.contains(")")) {
	            String name = subject.substring(0, subject.indexOf("(")).trim();
	            String countStr = subject.substring(subject.indexOf("(") + 1, subject.indexOf(")")).trim();
	            try {
	                result.put(name, Integer.parseInt(countStr));
	            } catch (NumberFormatException e) {
	                System.out.println("파싱 실패: " + subject);
	            }
	        }
	    }
	    return result;
	}
	private Boolean convertYnToBoolean(String ynValue) {
		if (ynValue == null) {
			return null;
		}
		return "Y".equalsIgnoreCase(ynValue.trim());
	}

}
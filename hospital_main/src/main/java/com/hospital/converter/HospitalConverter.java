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
import java.util.Objects;
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
				.doctorNum(hospitalMain.getDoctorNum())

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
				.professionalDoctors(convertProDocsToMap(hospitalMain.getProDocs())).build();
	}
	
	 /*private List<String> convertMedicalSubjectsToList(Set<MedicalSubject> subjects) {
	        if (subjects == null || subjects.isEmpty()) {
	            return List.of();
	        }

	        return subjects.stream()
	                .map(MedicalSubject::getSubjects)        // 엔티티의 subjects(String) 가져오기
	                .filter(s -> s != null && !s.isBlank())  // null/빈 문자열 제거
	                .flatMap(s -> Arrays.stream(s.split(","))) // 쉼표로 나누어 각 과목 분리
	                .map(String::trim)
	                .filter(s -> !s.isEmpty())
	                .distinct()
	                .sorted()
	                .collect(Collectors.toList());
	    }*/


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

		return hospitals.stream()
				.filter(Objects::nonNull)  // null 병원 객체 필터링
				.map(this::convertToDTO)
				.filter(Objects::nonNull)  // 변환 실패한 DTO 필터링
				.collect(Collectors.toList());
	}
	
	/**
	 * ProDoc Set을 Map으로 변환 (NullPointerException 방지)
	 * @param set ProDoc Set
	 * @return subjectName을 key로, proDocCount를 value로 하는 Map
	 */
	private Map<String, Integer> convertProDocsToMap(Set<ProDoc> set) {
		if (set == null || set.isEmpty()) {
			return new HashMap<>();
		}
		
		return set.stream()
				.filter(Objects::nonNull)                           // null 객체 필터링
				.filter(proDoc -> proDoc.getSubjectName() != null)   // null subjectName 필터링
				.filter(proDoc -> !proDoc.getSubjectName().trim().isEmpty())  // 빈 문자열 필터링
				.filter(proDoc -> proDoc.getProDocCount() != null)   // null proDocCount 필터링
				.filter(proDoc -> proDoc.getProDocCount() >= 0)      // 음수 필터링
				.collect(Collectors.toMap(
					proDoc -> proDoc.getSubjectName().trim(),        // key: 공백 제거된 과목명
					ProDoc::getProDocCount,                          // value: 전문의 수
					Integer::sum,                                    // 중복 시 합산
					HashMap::new                                     // HashMap 사용
				));
	}

	/**
	 * MedicalSubject Set을 List로 변환 (안전성 강화)
	 * @param set MedicalSubject Set
	 * @return 중복 제거되고 정렬된 과목명 리스트
	 */
	private List<String> convertMedicalSubjectsToList(Set<MedicalSubject> set) {
		if (set == null || set.isEmpty()) {
			return List.of();
		}
		
		return set.stream()
				.filter(Objects::nonNull)                          // null 객체 필터링
				.map(MedicalSubject::getSubjectName)               // 과목명 추출
				.filter(Objects::nonNull)                          // null 과목명 필터링
				.map(String::trim)                                 // 공백 제거
				.filter(name -> !name.isEmpty())                   // 빈 문자열 필터링
				.distinct()                                        // 중복 제거
				.sorted()                                          // 정렬
				.collect(Collectors.toList());
	}
	
	//ProDoc의 subjectDetails 문자열을 Map으로 변환
	/*private Map<String, Integer> convertProDocsToMap(Set<ProDoc> set) {
		if (set == null || set.isEmpty()) {
			return new HashMap<>();
		}

		return set.stream()
				.filter(proDoc -> proDoc.getSubjectDetails() != null)
				.flatMap(proDoc -> convertStringToSubjectMap(proDoc.getSubjectDetails()).entrySet().stream())
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					Integer::sum // 중복 시 합산
				));
	}*/

	//문자열 파싱 메서드: "내과(5명), 외과(3명)" → Map<String, Integer>
	/*private Map<String, Integer> convertStringToSubjectMap(String subjectDetails) {
		Map<String, Integer> result = new HashMap<>();
		if (subjectDetails == null || subjectDetails.trim().isEmpty()) {
			return result;
		}
		
		// "내과(5명), 외과(3명)" → Map으로 변환
		String[] subjects = subjectDetails.split(", ");
		for (String subject : subjects) {
			// "내과(5명)" → "내과"=5
			if (subject.contains("(") && subject.contains("명)")) {
				String name = subject.substring(0, subject.indexOf("(")).trim();
				String countStr = subject.substring(subject.indexOf("(") + 1, subject.indexOf("명)")).trim();
				try {
					result.put(name, Integer.parseInt(countStr));
				} catch (NumberFormatException e) {
					// 파싱 실패 시 무시
				}
			}
		}
		return result;
	}*/
	
	/**
	 * Y/N 문자열을 Boolean으로 변환
	 * @param ynValue Y/N 문자열
	 * @return Boolean 값 (null 안전)
	 */
	private Boolean convertYnToBoolean(String ynValue) {
        if (ynValue == null || ynValue.trim().isEmpty()) {
            return null;
        }
        return "Y".equalsIgnoreCase(ynValue.trim());
    }
}

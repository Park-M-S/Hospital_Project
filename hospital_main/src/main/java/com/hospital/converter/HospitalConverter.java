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
				.totalDoctors(hospitalMain.getTotalDoctors())

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
	
	private Map<String, Integer> convertProDocsToMap(Set<ProDoc> set) {
        if (set == null || set.isEmpty()) {
            return Map.of();
        }

        Map<String, Integer> result = new HashMap<>();
        for (ProDoc p : set) {
            putIfValid(result, "외과 일반의", p.getMedGeneralCnt());
            putIfValid(result, "외과 인턴", p.getMedInternCnt());
            putIfValid(result, "외과 레지던트", p.getMedResidentCnt());
            putIfValid(result, "외과 전문의", p.getMedSpecialistCnt());

            putIfValid(result, "치과 일반의", p.getDentGeneralCnt());
            putIfValid(result, "치과 인턴", p.getDentInternCnt());
            putIfValid(result, "치과 레지던트", p.getDentResidentCnt());
            putIfValid(result, "치과 전문의", p.getDentSpecialistCnt());

            putIfValid(result, "한방 일반의", p.getOrientalGeneralCnt());
            putIfValid(result, "한방 인턴", p.getOrientalInternCnt());
            putIfValid(result, "한방 레지던트", p.getOrientalResidentCnt());
            putIfValid(result, "한방 전문의", p.getOrientalSpecialistCnt());

            putIfValid(result, "조산사", p.getMidwifeCnt());
        }
        return result;
    }

    private void putIfValid(Map<String, Integer> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            try {
                int count = Integer.parseInt(value);
                if (count > 0) {
                    map.put(key, count);
                }
            } catch (NumberFormatException ignored) {}
        }
    }
	
	 private List<String> convertMedicalSubjectsToList(Set<MedicalSubject> subjects) {
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
	    }
	
	 /* private List<String> convertMedicalSubjectsToList(Set<MedicalSubject> set) {
	        if (set == null || set.isEmpty()) {
	            return List.of();
	        }
	        return set.stream()
	                .map(MedicalSubject::getSubjectName)
	                .filter(name -> name != null && !name.trim().isEmpty())
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

		return hospitals.stream().map(this::convertToDTO).collect(Collectors.toList());
	}
	
	 /*private Map<String, Integer> convertProDocsToMap(Set<ProDoc> set) {
	        if (set == null || set.isEmpty()) {
	            return new HashMap<>();
	        }
	        return set.stream()
	                .collect(Collectors.toMap(
	                    ProDoc::getSubjectName,
	                    ProDoc::getProDocCount,
	                    Integer::sum  // 중복 시 합산
	                ));
	    }*/
	

	//문자열 파싱 메서드: "내과(5명), 외과(3명)" → Map<String, Integer>
	private Map<String, Integer> convertStringToSubjectMap(String ProDocList) {
		Map<String, Integer> result = new HashMap<>();
		if (ProDocList == null || ProDocList.trim().isEmpty()) {
			return result;
		}
		
		// "내과(5명), 외과(3명)" → Map으로 변환
		String[] subjects = ProDocList.split(", ");
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
	}
	
	private Boolean convertYnToBoolean(String ynValue) {
        if (ynValue == null) {
            return null;
        }
        return "Y".equalsIgnoreCase(ynValue.trim());
    }
	
	


}
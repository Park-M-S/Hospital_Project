package com.hospital.converter;

import com.hospital.entity.HospitalMain;
import com.hospital.entity.MedicalSubject;
import com.hospital.dto.HospitalWebResponse;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.ProDoc;
import com.hospital.util.TodayOperatingTimeCalculator;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
				.parkingFee(detail != null ? convertParkingFeeToBoolean(detail.getParkXpnsYn()) : null)

				// 운영 시간
				.todayOpen(formatTime(todayTime.getOpenTime())).todayClose(formatTime(todayTime.getCloseTime()))

				.medicalSubject(convertMedicalSubjectsToList(hospitalMain.getMedicalSubjects()))

				// 전문의 정보를 문자열로 변환
				.professionalDoctors(convertProDocsToSet(hospitalMain.getProDocs())).build();
	}

	private List<String> convertMedicalSubjectsToList(Set<MedicalSubject> set) {
		if (set == null || set.isEmpty()) {
			return List.of(); // 비어있는 리스트 반환
		}
		return set.stream().map(MedicalSubject::getSubjectName).filter(name -> name != null && !name.trim().isEmpty())
				.distinct().sorted().collect(Collectors.toList());
	}

	private String formatTime(String timeStr) {
		// null이거나 4자리가 아니면 원본값 그대로 반환
		if (timeStr == null || timeStr.length() != 4) {
			return timeStr;
		}

		// HHMM을 HH:MM으로 변환
		return timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4);
	}
	
	private Boolean convertYnToBoolean(String ynValue) {
		if (ynValue == null) {
			return null;
		}
		return "Y".equalsIgnoreCase(ynValue.trim());
	}

	// Hospital 엔티티 리스트를 DTO 리스트로 변환
	public List<HospitalWebResponse> convertToDtos(List<HospitalMain> hospitals) {
		if (hospitals == null) {
			return List.of();
		}

		return hospitals.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	private Set<String> convertProDocsToSet(Set<ProDoc> set) {
		if (set == null || set.isEmpty()) {
			return new HashSet<>();
		}
		
		return set.stream()
				.map(proDoc -> proDoc.getSubjectName() + ": " + proDoc.getProDocCount())
				.collect(Collectors.toSet());
	}
	private Boolean convertParkingFeeToBoolean(String parkXpnsYn) {
	    if (parkXpnsYn == null) {
	        return null;
	    }
	    return "Y".equalsIgnoreCase(parkXpnsYn.trim());
	}
}

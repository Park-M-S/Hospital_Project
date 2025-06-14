package com.hospital.converter;

import com.hospital.dto.web.HospitalResponseDTO;
import com.hospital.entity.HospitalMain;
import com.hospital.entity.MedicalSubject;
import com.hospital.entity.HospitalDetail;
import com.hospital.entity.ProDoc;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HospitalConverter {
    
    /**
     * Hospital 엔티티를 HospitalResponseDto로 변환
     */
    public HospitalResponseDTO convertToDTO(HospitalMain hospitalMain) {
        if (hospitalMain == null) {
            return null;
        }
        
        HospitalDetail detail = hospitalMain.getHospitalDetail();
       
        
        return HospitalResponseDTO.builder()
            // 기본 정보
            .hospitalName(hospitalMain.getHospitalName())
            .hospitalAddress(hospitalMain.getHospitalAddress())
            .provinceName(hospitalMain.getProvinceName())
            .districtName(hospitalMain.getDistrictName())
            .hospitalTel(hospitalMain.getHospitalTel())
            .hospitalHomepage(hospitalMain.getHospitalHomepage())
            .doctorNum(hospitalMain.getDoctorNum())
            
            // 좌표 정보
            .coordinateX(hospitalMain.getCoordinateX())
            .coordinateY(hospitalMain.getCoordinateY())
            
            // 운영 정보 (detail이 있을 때만)
            .emergencyDayAvailable(detail != null ? detail.getEmyDayYn() : null)
            .emergencyNightAvailable(detail != null ? detail.getEmyNightYn() : null)
            .weekdayLunch(detail != null ? detail.getLunchWeek() : null)
            .parkingCapacity(detail != null ? detail.getParkQty() : null)
            .parkingFee(detail != null ? detail.getParkXpnsYn() : null)
            
            
            // 요일별 운영시간
            .mondayOpen(detail != null ? detail.getTrmtMonStart() : null)
            .mondayClose(detail != null ? detail.getTrmtMonEnd() : null)
            .tuesdayOpen(detail != null ? detail.getTrmtTueStart() : null)
            .tuesdayClose(detail != null ? detail.getTrmtTueEnd() : null)
            .wednesdayOpen(detail != null ? detail.getTrmtWedStart() : null)
            .wednesdayClose(detail != null ? detail.getTrmtWedEnd() : null)
            .thursdayOpen(detail != null ? detail.getTrmtThurStart() : null)
            .thursdayClose(detail != null ? detail.getTrmtThurEnd() : null)
            .fridayOpen(detail != null ? detail.getTrmtFriStart() : null)
            .fridayClose(detail != null ? detail.getTrmtFriEnd() : null)
            .saturdayOpen(detail != null ? detail.getTrmtSatStart() : null)
            .saturdayClose(detail != null ? detail.getTrmtSatEnd() : null)
            .sundayOpen(detail != null ? detail.getTrmtSunStart() : null)
            .sundayClose(detail != null ? detail.getTrmtSunEnd() : null)
            
            .medicalSubject(convertMedicalSubjectsToString(hospitalMain.getMedicalSubjects()))
            
         
            // 전문의 정보를 문자열로 변환
            .professionalDoctors(convertProDocsToString(hospitalMain.getProDocs()))
            .build();
    }
    
    private String convertMedicalSubjectsToString(List<MedicalSubject> medicalSubjects) {
        if (medicalSubjects == null || medicalSubjects.isEmpty()) {
            return null;
        }
        return medicalSubjects.stream()
                .map(MedicalSubject::getSubjectName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .distinct()  // 중복 제거
                .sorted()    // 정렬
                .collect(Collectors.joining(", "));
    }
    
    /**
     * Hospital 엔티티 리스트를 DTO 리스트로 변환
     */
    public List<HospitalResponseDTO> convertToDtos(List<HospitalMain> hospitals) {
        if (hospitals == null) {
            return List.of();
        }
        
        return hospitals.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * ProDoc 리스트를 문자열로 변환
     * 형태: "안과:1|이비인후과:1|비뇨의학과:1"
     */
    private String convertProDocsToString(List<ProDoc> proDocs) {
        if (proDocs == null || proDocs.isEmpty()) {
            return null;
        }
        
        return proDocs.stream()
                .filter(proDoc -> proDoc.getSubjectName() != null && proDoc.getProDocCount() != null)
                .map(proDoc -> proDoc.getSubjectName() + ": " + proDoc.getProDocCount()) // 콜론 뒤에 공백 추가
                .collect(Collectors.joining(", ")); // 쉼표 + 공백으로 구분
        }
}
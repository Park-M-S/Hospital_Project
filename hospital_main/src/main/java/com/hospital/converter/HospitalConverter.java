package com.hospital.converter;

import com.hospital.dto.HospitalDTO;
import com.hospital.entity.HospitalEntity;

public class HospitalConverter {
	public static HospitalDTO convertToDTO(HospitalEntity hospitalEntity) {
        return new HospitalDTO(
                hospitalEntity.getHospitalName(),
                hospitalEntity.getHospitalSubject(),
                hospitalEntity.getHospitalAddress(),
                hospitalEntity.getCoordinateX(),
                hospitalEntity.getCoordinateY(),
                hospitalEntity.getEmergencyAvailable(),
                hospitalEntity.getParkAvailable(),
                hospitalEntity.getProDoc()
        );
    }

}

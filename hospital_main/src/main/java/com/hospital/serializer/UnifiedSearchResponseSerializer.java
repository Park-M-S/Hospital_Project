package com.hospital.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hospital.dto.UnifiedSearchResponse;

public class UnifiedSearchResponseSerializer extends StdSerializer<UnifiedSearchResponse> {

    public UnifiedSearchResponseSerializer() {
        this(null);
    }

    public UnifiedSearchResponseSerializer(Class<UnifiedSearchResponse> t) {
        super(t);
    }

    @Override
    public void serialize(UnifiedSearchResponse value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        String type = value.getMedicalType();

        if ("HOSPITAL".equals(type)) {
            // 병원 필드만 직렬화
            writeIfNotNull(gen, "medicalType", value.getMedicalType());
            writeIfNotNull(gen, "hospitalCode", value.getHospitalCode());
            writeIfNotNull(gen, "hospitalName", value.getName());
            writeIfNotNull(gen, "hospitalAddress", value.getAddress());
            writeIfNotNull(gen, "hospitalTel", value.getTel());
            writeIfNotNull(gen, "coordinateX", value.getCoordinateX());
            writeIfNotNull(gen, "coordinateY", value.getCoordinateY());
     
            writeIfNotNull(gen, "totalDoctors", value.getTotalDoctors());
 
            writeIfNotNull(gen, "weekdayLunch", value.getWeekdayLunch());
            writeIfNotNull(gen, "parkingCapacity", value.getParkingCapacity());
            writeIfNotNull(gen, "parkingFee", value.getParkingFee());
            writeIfNotNull(gen, "todayOpen", value.getTodayOpen());
            writeIfNotNull(gen, "todayClose", value.getTodayClose());
            writeIfNotNull(gen, "noTrmtHoli", value.getNoTrmtHoli());
            writeIfNotNull(gen, "noTrmtSun", value.getNoTrmtSun());
            writeIfNotNull(gen, "medicalSubjects", value.getMedicalSubjects());
            writeIfNotNull(gen, "professionalDoctors", value.getProfessionalDoctors());

        } else if ("PHARMACY".equals(type)) {
            // 약국 필드만 직렬화
            writeIfNotNull(gen, "medicalType", value.getMedicalType());
            writeIfNotNull(gen, "pharmacyCode", value.getPharmacyCode());
            writeIfNotNull(gen, "pharmacyName", value.getName());
            writeIfNotNull(gen, "pharmacyAddress", value.getAddress());
            writeIfNotNull(gen, "PharmacyTel", value.getTel());
            writeIfNotNull(gen, "coordinateX", value.getCoordinateX());
            writeIfNotNull(gen, "coordinateY", value.getCoordinateY());

        } else if ("EMERGENCY".equals(type)) {
            // 응급실 필드만 직렬화
            writeIfNotNull(gen, "medicalType", value.getMedicalType());
            writeIfNotNull(gen, "dutyName", value.getName());
            writeIfNotNull(gen, "emergencyAddress", value.getAddress());
            writeIfNotNull(gen, "dutyTel3", value.getTel());
            writeIfNotNull(gen, "coordinateX", value.getCoordinateX());
            writeIfNotNull(gen, "coordinateY", value.getCoordinateY());
            writeIfNotNull(gen, "hpid", value.getHpid());
            writeIfNotNull(gen, "hvidate", value.getHvidate());
            writeIfNotNull(gen, "hvamyn", value.getHvamyn());
            writeIfNotNull(gen, "availableEquipment", value.getAvailableEquipment());
            writeIfNotNull(gen, "availableBeds", value.getAvailableBeds());
        }

        gen.writeEndObject();
    }

    private void writeIfNotNull(JsonGenerator gen, String fieldName, Object value) throws IOException {
        gen.writeObjectField(fieldName, value);
    }
}

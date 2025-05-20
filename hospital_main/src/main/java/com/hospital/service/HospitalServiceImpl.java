package com.hospital.service;

import com.hospital.entity.HospitalEntity;
import com.hospital.repository.HospitalRepository;
import com.hospital.dto.HospitalDTO;
import com.hospital.converter.HospitalConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository HospitalRepository;
    private final HospitalFilter hospitalFilter;
  

    @Autowired
    public HospitalServiceImpl(HospitalRepository HospitalRepository, HospitalFilter hospitalFilter) {
        this.HospitalRepository = HospitalRepository;
        this.hospitalFilter = hospitalFilter;
    
    }

    @Override
    public List<HospitalDTO> getHospitals(String sub, double userLat, double userLng, double radius, List<String> tags) {

        List<HospitalEntity> hospitalEntities = HospitalRepository.getAllHospitals(sub);

        List<HospitalDTO> hospitals = hospitalEntities.stream()        
                .filter(hospitalEntity -> hospitalEntity.matchesTags(tags))
                .map(HospitalConverter::convertToDTO) // HospitalEntity를 HospitalDTO로 변환
                .filter(hospital -> hospitalFilter.filterByDistance(hospital, userLat, userLng, radius))
                .collect(Collectors.toList());

        return hospitals;
    }

}
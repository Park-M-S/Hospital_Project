package com.hospital.service;

import com.hospital.entity.HospitalMain;
import com.hospital.repository.HospitalRepository;
import com.hospital.util.DistanceCalculator;
import com.hospital.converter.HospitalConverter;
import com.hospital.domainLogic.HospitalTagFilter;
import com.hospital.dto.web.HospitalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HospitalServiceImpl implements HospitalService {
    
    private final HospitalRepository hospitalRepository;
    private final HospitalFilter hospitalFilter;
    private final HospitalConverter hospitalConverter;
    private final DistanceCalculator distanceCalculator;
    
    @Autowired
    public HospitalServiceImpl(HospitalRepository hospitalRepository, HospitalFilter hospitalFilter,
            HospitalConverter hospitalConverter,
            DistanceCalculator distanceCalculator) {
        this.hospitalRepository = hospitalRepository;
        this.hospitalFilter = hospitalFilter;
        this.hospitalConverter = hospitalConverter;
        this.distanceCalculator = distanceCalculator;
    }
    
    // 기존 메서드: 진료과목으로 병원 검색
    @Override
    public List<HospitalResponse> getHospitals(List<String> subs, double userLat, double userLng, double radius, List<String> tags) {
        List<HospitalMain> hospitalEntities = hospitalRepository.findHospitalsBySubjects(subs);
        return applyFiltersAndSort(hospitalEntities, userLat, userLng, radius, tags);
    }
    
    // ✅ 병원명 검색 
    @Override
    public List<HospitalResponse> searchHospitalsByName(String hospitalName) {
        // 입력값 전처리
        String cleanInput = hospitalName.replace(" ", "");
        
        // Repository에서 검색 (hospitalDetail + medicalSubjects EAGER FETCH)
        List<HospitalMain> hospitalEntities = hospitalRepository.findHospitalsByName(cleanInput);
        
        // 단순히 DTO로 변환해서 리턴
        return hospitalEntities.stream()
                .map(hospitalConverter::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // ✅ 공통 로직: 필터링 + 정렬
    private List<HospitalResponse> applyFiltersAndSort(
            List<HospitalMain> hospitalEntities,
            double userLat, 
            double userLng, 
            double radius, 
            List<String> tags) {
        
        return hospitalEntities.stream()
                .filter(hospital -> HospitalTagFilter.matchesAllTags(hospital, tags))
                .map(hospitalConverter::convertToDTO)
                .filter(dto -> hospitalFilter.filterByDistance(dto, userLat, userLng, radius)) // HospitalFilter 사용
                .map(dto -> {
                    // 거리 계산해서 임시 객체 생성 (정렬용)
                    double distance = distanceCalculator.calculateDistance(
                            userLat, userLng, dto.getCoordinateY(), dto.getCoordinateX());
                    return new HospitalWithDistance(dto, distance);
                })
                .sorted((h1, h2) -> Double.compare(h1.distance, h2.distance)) // 거리순 정렬
                .map(hwd -> hwd.hospital) // 다시 HospitalResponseDTO만 추출
                .collect(Collectors.toList());
    }
    
    // 거리와 함께 임시로 저장하는 내부 클래스
    private static class HospitalWithDistance {
        final HospitalResponse hospital;
        final double distance;
        
        HospitalWithDistance(HospitalResponse hospital, double distance) {
            this.hospital = hospital;
            this.distance = distance;
        }
    }
}
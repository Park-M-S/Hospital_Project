package com.hospital.service;

import com.hospital.client.PharmacyApiCaller;
import com.hospital.dto.api.OpenApiWrapper;
import com.hospital.dto.api.PharmacyApiItem;
import com.hospital.entity.Pharmacy;
import com.hospital.repository.PharmacyApiRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyApiServiceImpl implements PharmacyApiService {

    private final PharmacyApiCaller apiCaller;
    private final PharmacyApiRepository pharmacyApirepository;

    @Override
    public int fetchAndSaveByDistrict(String sgguCd) {
    	pharmacyApirepository.deleteAll();
    	
        OpenApiWrapper.Body body = apiCaller.callApiByDistrict(sgguCd);

        // ✅ 예외 처리: 응답 또는 아이템이 비어있는 경우
        if (body == null || body.getItems() == null || body.getItems().isEmpty()) {
            log.warn("📭 [{}] 지역에 저장할 약국 정보 없음", sgguCd);
            return 0;
        }

        List<PharmacyApiItem> items = body.getItems();

        // ✅ 로그 추가: 파싱된 아이템 수 확인
        log.info("📦 [{}] 지역 파싱된 약국 수: {}", sgguCd, items.size());

        // ✅ 로그 추가: 각 약국 상세 정보 확인
        for (PharmacyApiItem item : items) {
            log.info("🧾 약국명: {}, 주소: {}, 전화번호: {}, 위도: {}, 경도: {}, Ykiho: {}",
                    item.getYadmNm(), item.getAddr(), item.getTelno(),
                    item.getYPos(), item.getXPos(), item.getYkiho());
        }

        int savedCount = 0;

        for (PharmacyApiItem item : items) {
            // 중복 여부 확인
            if (pharmacyApirepository.existsByYkiho(item.getYkiho())) {
                continue;
            }

            // DTO → Entity 매핑
            Pharmacy pharmacy = new Pharmacy();
            pharmacy.setName(item.getYadmNm());
            pharmacy.setAddress(item.getAddr());
            pharmacy.setPhone(item.getTelno());
            pharmacy.setLatitude(item.getYPos());
            pharmacy.setLongitude(item.getXPos());
            pharmacy.setYkiho(item.getYkiho());

            // 저장
            pharmacyApirepository.save(pharmacy);
            savedCount++;
            
            
        }

        // ✅ 최종 저장 완료 로그
        log.info("✅ [{}] 지역 약국 {}건 저장 완료", sgguCd, savedCount);
        return savedCount;
        
        
        
        
    }
}

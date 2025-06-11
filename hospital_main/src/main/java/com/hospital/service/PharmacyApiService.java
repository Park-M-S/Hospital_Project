package com.hospital.service;

public interface PharmacyApiService {

    /**
     * ✅ 지정된 구 이름으로 약국 API 호출 → 파싱 → 저장
     * @param district 예: "성남시 분당구"
     * @return 저장된 약국 수
     */
    int fetchAndSaveByDistrict(String district);
}

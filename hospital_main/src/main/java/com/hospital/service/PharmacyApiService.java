package com.hospital.service;

import com.hospital.async.PharmacyAsyncRunner;
import com.hospital.config.RegionConfig;
import com.hospital.repository.PharmacyApiRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PharmacyApiService {

    private final PharmacyAsyncRunner pharmacyAsyncRunner;
    private final PharmacyApiRepository pharmacyApiRepository;
    private final RegionConfig regionConfig;

    @Autowired
    public PharmacyApiService(PharmacyAsyncRunner pharmacyAsyncRunner,
                              PharmacyApiRepository pharmacyApiRepository,
                              RegionConfig regionConfig) {
        this.pharmacyAsyncRunner = pharmacyAsyncRunner;
        this.pharmacyApiRepository = pharmacyApiRepository;
        this.regionConfig = regionConfig;
    }

    /**
     * 전국 약국 데이터 수집 (비동기 처리)
     */
    public int savePharmacy() {
        log.info("전국 약국 데이터 수집 시작");

        // 기존 데이터 삭제
        pharmacyApiRepository.deleteAllPharmacies();
        pharmacyApiRepository.resetAutoIncrement();
        log.info("기존 약국 데이터 전체 삭제 완료");

        // 전국 시도코드 가져오기
        List<String> sidoCodes = regionConfig.getNationwideSidoCodes();
        
        // 카운터 초기화 및 비동기 실행
        pharmacyAsyncRunner.setTotalCount(sidoCodes.size());
        
        for (String sidoCd : sidoCodes) {
            pharmacyAsyncRunner.runAsync(sidoCd);
        }

        log.info("전국 {}개 시도 비동기 처리 시작", sidoCodes.size());
        return sidoCodes.size();    }

    public int getCompletedCount() {
		return pharmacyAsyncRunner.getCompletedCount();
	}

	public int getFailedCount() {
		return pharmacyAsyncRunner.getFailedCount();
	}
}
package com.hospital.scheduler.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hospital.service.PharmacyApiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PharmacyJob implements Job {
    
    @Autowired
    private PharmacyApiService pharmacyApiService;
    
    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("🏥 약국 정보 수집 시작");
            int count = pharmacyApiService.fetchAndSaveSeongnamPharmacies();
            log.info("✅ 약국 정보 수집 완료: {}건", count);
        } catch (Exception e) {
            log.error("❌ 약국 정보 수집 실패", e);
        }
    }
}
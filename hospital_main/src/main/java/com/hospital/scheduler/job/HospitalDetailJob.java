package com.hospital.scheduler.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hospital.service.HospitalDetailApiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HospitalDetailJob implements Job {
    
    @Autowired
    private HospitalDetailApiService hospitalDetailApiService;
    
    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("🏥 병원 상세정보 수집 시작");
            int count = hospitalDetailApiService.updateAllHospitalDetails();
            log.info("✅ 병원 상세정보 수집 시작: {}건 처리 예정", count);
        } catch (Exception e) {
            log.error("❌ 병원 상세정보 수집 실패", e);
        }
    }
}
package com.hospital.job;

import com.hospital.service.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// ✅ 1. 병원 기본정보 수집 Job
@Slf4j
@Component
public class HospitalMainJob implements Job {
    
    @Autowired
    private HospitalMainApiService hospitalMainApiService;
    
    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("🏥 병원 기본정보 수집 시작");
            int count = hospitalMainApiService.fetchParseAndSaveHospitals();
            log.info("✅ 병원 기본정보 수집 완료: {}건", count);
        } catch (Exception e) {
            log.error("❌ 병원 기본정보 수집 실패", e);
        }
    }
}
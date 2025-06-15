package com.hospital.scheduler.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hospital.service.ProDocApiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProDocJob implements Job {
    
    @Autowired
    private ProDocApiService proDocApiService;
    
    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("🏥 전문의 정보 수집 시작");
            int count = proDocApiService.fetchParseAndSaveProDocs();
            log.info("✅ 전문의 정보 수집 시작: {}건 처리 예정", count);
        } catch (Exception e) {
            log.error("❌ 전문의 정보 수집 실패", e);
        }
    }
}
package com.hospital.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hospital.service.MedicalSubjectApiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MedicalSubjectJob implements Job {
    
    @Autowired
    private MedicalSubjectApiService medicalSubjectApiService;
    
    @Override
    public void execute(JobExecutionContext context) {
        try {
            log.info("진료과목 정보 수집 시작");
            int count = medicalSubjectApiService.updateSubjects();
            log.info("진료과목 정보 수집 시작: {}건 처리 예정", count);
        } catch (Exception e) {
            log.error("진료과목 정보 수집 실패", e);
        }
    }
}

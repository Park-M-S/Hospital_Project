package com.hospital.service;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hospital.job.HospitalDetailJob;
import com.hospital.job.HospitalMainJob;
import com.hospital.job.MedicalSubjectJob;
import com.hospital.job.PharmacyJob;
import com.hospital.job.ProDocJob;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QuartzSchedulerService {

	private final Scheduler scheduler;

	public QuartzSchedulerService(@Qualifier("scheduler")Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@PostConstruct
	public void initSchedule() {
		try {
			scheduler.clear();

			// 1. 병원 기본정보: 매일 새벽 2시
			scheduleJob(HospitalMainJob.class, "hospitalMainJob", "0 0 2 * * ?");
			// 2. 병원 상세정보: 매일 새벽 2시 20분
			scheduleJob(HospitalDetailJob.class, "hospitalDetailJob", "0 20 2 * * ?");
			// 3. 진료과목: 매일 새벽 2시 40분
			scheduleJob(MedicalSubjectJob.class, "medicalSubjectJob", "0 40 2 * * ?");
			// 4. 전문의 정보: 매일 새벽 3시
			scheduleJob(ProDocJob.class, "proDocJob", "0 0 3 * * ?");
			// 5. 약국 정보: 매일 새벽 3시 20분
			scheduleJob(PharmacyJob.class, "pharmacyJob", "0 20 3 * * ?");

			log.info("✅ Quartz 스케줄러 초기화 완료");

		} catch (SchedulerException e) {
			log.error("❌ Quartz 스케줄러 초기화 실패", e);
		}
	}

	private void scheduleJob(Class<? extends Job> jobClass, String jobName, String cronExpression)
			throws SchedulerException {

		JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName).build();

		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName + "Trigger")
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

		scheduler.scheduleJob(jobDetail, trigger);
		log.info("📅 스케줄 등록: {} - {}", jobName, cronExpression);
	}

	// 수동 실행 메서드들
	public void runHospitalMainJob() throws SchedulerException {
		JobKey jobKey = new JobKey("hospitalMainJob");
		scheduler.triggerJob(jobKey);
		log.info("🔥 병원 기본정보 수집 수동 실행");
	}

	public void runHospitalDetailJob() throws SchedulerException {
		JobKey jobKey = new JobKey("hospitalDetailJob");
		scheduler.triggerJob(jobKey);
		log.info("🔥 병원 상세정보 수집 수동 실행");
	}

	public void runMedicalSubjectJob() throws SchedulerException {
		JobKey jobKey = new JobKey("medicalSubjectJob");
		scheduler.triggerJob(jobKey);
		log.info("🔥 진료과목 정보 수집 수동 실행");
	}

	public void runProDocJob() throws SchedulerException {
		JobKey jobKey = new JobKey("proDocJob");
		scheduler.triggerJob(jobKey);
		log.info("🔥 전문의 정보 수집 수동 실행");
	}

	public void runPharmacyJob() throws SchedulerException {
		JobKey jobKey = new JobKey("pharmacyJob");
		scheduler.triggerJob(jobKey);
		log.info("🔥 약국 정보 수집 수동 실행");
	}
}
package com.hospital.config;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzConfig {

    @Bean(destroyMethod = "destroy")
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(new SpringBeanJobFactory());
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(false);
        factory.setWaitForJobsToCompleteOnShutdown(true);

        // Quartz 쓰레드 풀 설정 - 최소화
        java.util.Properties quartzProperties = new java.util.Properties();
        quartzProperties.setProperty("org.quartz.threadPool.threadCount", "1"); // 쓰레드 1개로 최소화
        quartzProperties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        factory.setQuartzProperties(quartzProperties);

        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        return scheduler;
    }
}
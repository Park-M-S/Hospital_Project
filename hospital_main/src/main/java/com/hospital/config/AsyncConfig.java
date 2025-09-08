package com.hospital.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


@Configuration
public class AsyncConfig {

    /**
     * API 호출용 스레드 풀 (병원 데이터 수집용)
     */
    @Bean(name = "apiExecutor")
    public Executor apiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);           // 기본 스레드 수
        executor.setMaxPoolSize(15);           // 최대 스레드 수
        executor.setQueueCapacity(100);        // 큐 용량
        executor.setThreadNamePrefix("Api-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        System.out.println("API Executor 설정 완료 (코어: 5, 최대: 10)");
        return executor;
    }

    /**
     * 응급실 데이터용 일반 스레드 풀
     */
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // 응급실용은 적은 수로
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Emergency-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        System.out.println("Task Executor 설정 완료 (코어: 2, 최대: 5)");
        return executor;
    }

    /**
     * 스케줄링용 TaskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);              // 스케줄링용 스레드 수
        scheduler.setThreadNamePrefix("Emergency-Scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        
        System.out.println("Task Scheduler 설정 완료 (풀 크기: 3)");
        return scheduler;
    }
}

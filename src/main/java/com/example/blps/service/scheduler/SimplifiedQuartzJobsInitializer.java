package com.example.blps.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Инициализатор заданий для планировщика Quartz
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SimplifiedQuartzJobsInitializer {

    private final org.quartz.Scheduler scheduler;
    
    /**
     * Инициализирует задания Quartz при запуске приложения
     */
    @jakarta.annotation.PostConstruct
    public void initializeJobs() {
        log.info("Initializing Quartz jobs");
        
        try {
            // Планирование задания для еженедельного отчета (каждый понедельник в 8:00)
            org.quartz.JobDetail weeklyJobDetail = org.quartz.JobBuilder.newJob(WeeklyDashboardJob.class)
                    .withIdentity("weeklyDashboardJob", "dashboardGroup")
                    .storeDurably()
                    .build();

            org.quartz.CronTrigger weeklyTrigger = org.quartz.TriggerBuilder.newTrigger()
                    .withIdentity("weeklyDashboardTrigger", "dashboardGroup")
                    .withSchedule(org.quartz.CronScheduleBuilder.cronSchedule("0 0 8 ? * MON"))
                    .forJob(weeklyJobDetail)
                    .build();

            scheduler.scheduleJob(weeklyJobDetail, weeklyTrigger);
            log.info("Weekly dashboard job scheduled");
            
            // Планирование задания для ежемесячного отчета (1-го числа каждого месяца в 8:00)
            org.quartz.JobDetail monthlyJobDetail = org.quartz.JobBuilder.newJob(MonthlyDashboardJob.class)
                    .withIdentity("monthlyDashboardJob", "dashboardGroup")
                    .storeDurably()
                    .build();

            org.quartz.CronTrigger monthlyTrigger = org.quartz.TriggerBuilder.newTrigger()
                    .withIdentity("monthlyDashboardTrigger", "dashboardGroup")
                    .withSchedule(org.quartz.CronScheduleBuilder.cronSchedule("0 0 8 1 * ?"))
                    .forJob(monthlyJobDetail)
                    .build();

            scheduler.scheduleJob(monthlyJobDetail, monthlyTrigger);
            log.info("Monthly dashboard job scheduled");
            
            log.info("Quartz jobs initialized successfully");
            
        } catch (org.quartz.SchedulerException e) {
            log.error("Error scheduling Quartz jobs", e);
        }
    }
}
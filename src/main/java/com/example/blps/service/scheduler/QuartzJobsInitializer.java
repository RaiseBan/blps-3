package com.example.blps.service.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuartzJobsInitializer {

    private final Scheduler scheduler;
    
    @Value("${notification.dashboard.enabled:true}")
    private boolean dashboardEnabled;
    
    @Value("${notification.dashboard.weekly.cron:0 0 8 ? * MON}")
    private String weeklyDashboardCron;
    
    @Value("${notification.dashboard.monthly.cron:0 0 8 1 * ?}")
    private String monthlyDashboardCron;
    
    @Value("${notification.campaign-analysis.cron:0 0 */4 * * ?}")
    private String campaignAnalysisCron;

    /**
     * Инициализирует задания Quartz при запуске приложения
     */
    @PostConstruct
    public void initializeJobs() {
        log.info("Initializing Quartz jobs");
        
        if (dashboardEnabled) {
            scheduleWeeklyDashboardJob();
            scheduleMonthlyDashboardJob();
        } else {
            log.info("Dashboard generation is disabled");
        }
        
        scheduleCampaignAnalysisJob();
        
        log.info("Quartz jobs initialized successfully");
    }

    /**
     * Планирует еженедельное задание для генерации дашборда
     */
    private void scheduleWeeklyDashboardJob() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(WeeklyDashboardJob.class)
                    .withIdentity("weeklyDashboardJob", "dashboardGroup")
                    .storeDurably()
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("weeklyDashboardTrigger", "dashboardGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule(weeklyDashboardCron))
                    .forJob(jobDetail)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Weekly dashboard job scheduled with cron: {}", weeklyDashboardCron);
            
        } catch (SchedulerException e) {
            log.error("Error scheduling weekly dashboard job", e);
        }
    }

    /**
     * Планирует ежемесячное задание для генерации дашборда
     */
    private void scheduleMonthlyDashboardJob() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(MonthlyDashboardJob.class)
                    .withIdentity("monthlyDashboardJob", "dashboardGroup")
                    .storeDurably()
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("monthlyDashboardTrigger", "dashboardGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule(monthlyDashboardCron))
                    .forJob(jobDetail)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Monthly dashboard job scheduled with cron: {}", monthlyDashboardCron);
            
        } catch (SchedulerException e) {
            log.error("Error scheduling monthly dashboard job", e);
        }
    }

    /**
     * Планирует задание для анализа кампаний
     */
    private void scheduleCampaignAnalysisJob() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(CampaignAnalysisJob.class)
                    .withIdentity("campaignAnalysisJob", "analyticsGroup")
                    .storeDurably()
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("campaignAnalysisTrigger", "analyticsGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule(campaignAnalysisCron))
                    .forJob(jobDetail)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Campaign analysis job scheduled with cron: {}", campaignAnalysisCron);
            
        } catch (SchedulerException e) {
            log.error("Error scheduling campaign analysis job", e);
        }
    }
}
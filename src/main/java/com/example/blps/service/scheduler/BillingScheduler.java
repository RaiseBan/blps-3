package com.example.blps.service.scheduler;

import com.example.blps.dto.billing.BillingRequest;
import com.example.blps.dto.billing.BillingType;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.service.billing.BillingSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

/**
 * Планировщик для автоматической генерации счетов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {
    
    private final Scheduler scheduler;
    
    @PostConstruct
    public void initializeJobs() {
        try {
            // Ежедневная генерация счетов в 00:00
            JobDetail dailyJob = JobBuilder.newJob(DailyBillingJob.class)
                    .withIdentity("dailyBillingJob", "billingGroup")
                    .storeDurably()
                    .build();
            
            Trigger dailyTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("dailyBillingTrigger", "billingGroup")
                    .withSchedule(
                            org.quartz.CronScheduleBuilder.cronSchedule("0 0 0 * * ?")
                    )
                    .build();
            
            scheduler.scheduleJob(dailyJob, dailyTrigger);
            log.info("Daily billing job scheduled");
            
            // Ежемесячная генерация счетов 1-го числа в 02:00
            JobDetail monthlyJob = JobBuilder.newJob(MonthlyBillingJob.class)
                    .withIdentity("monthlyBillingJob", "billingGroup")
                    .storeDurably()
                    .build();
            
            Trigger monthlyTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("monthlyBillingTrigger", "billingGroup")
                    .withSchedule(
                            org.quartz.CronScheduleBuilder.cronSchedule("0 0 2 1 * ?")
                    )
                    .build();
            
            scheduler.scheduleJob(monthlyJob, monthlyTrigger);
            log.info("Monthly billing job scheduled");
            
        } catch (Exception e) {
            log.error("Error scheduling billing jobs", e);
        }
    }
    
    /**
     * Job для ежедневной генерации счетов
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class DailyBillingJob implements Job {
        
        private final OurCampaignRepository campaignRepository;
        private final BillingSenderService billingSenderService;
        
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            log.info("Starting daily billing job");
            
            try {
                LocalDate yesterday = LocalDate.now().minusDays(1);
                List<OurCampaign> campaigns = campaignRepository.findAll();
                
                for (OurCampaign campaign : campaigns) {
                    BillingRequest request = BillingRequest.builder()
                            .campaignId(campaign.getId())
                            .periodStart(yesterday)
                            .periodEnd(yesterday)
                            .requestedBy("system-scheduler")
                            .billingType(BillingType.DAILY)
                            .build();
                    
                    billingSenderService.sendBillingRequest(request);
                }
                
                log.info("Daily billing job completed successfully");
            } catch (Exception e) {
                log.error("Error executing daily billing job", e);
                throw new JobExecutionException(e);
            }
        }
    }
    
    /**
     * Job для ежемесячной генерации счетов
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class MonthlyBillingJob implements Job {
        
        private final OurCampaignRepository campaignRepository;
        private final BillingSenderService billingSenderService;
        
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            log.info("Starting monthly billing job");
            
            try {
                LocalDate lastMonth = LocalDate.now().minusMonths(1);
                LocalDate startOfMonth = lastMonth.withDayOfMonth(1);
                LocalDate endOfMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
                
                List<OurCampaign> campaigns = campaignRepository.findAll();
                
                for (OurCampaign campaign : campaigns) {
                    BillingRequest request = BillingRequest.builder()
                            .campaignId(campaign.getId())
                            .periodStart(startOfMonth)
                            .periodEnd(endOfMonth)
                            .requestedBy("system-scheduler")
                            .billingType(BillingType.MONTHLY)
                            .build();
                    
                    billingSenderService.sendBillingRequest(request);
                }
                
                log.info("Monthly billing job completed successfully");
            } catch (Exception e) {
                log.error("Error executing monthly billing job", e);
                throw new JobExecutionException(e);
            }
        }
    }
}
package com.example.blps.service.scheduler;

import com.example.blps.service.geo.GeoBitrixSyncService;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Планировщик для синхронизации геоданных с Bitrix24
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeoDataSyncScheduler {
    
    private final Scheduler scheduler;
    
    @Value("${geo.sync.interval.minutes:5}")
    private int syncIntervalMinutes;
    
    @PostConstruct
    public void initializeJob() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(GeoDataSyncJob.class)
                    .withIdentity("geoDataSyncJob", "geoGroup")
                    .storeDurably()
                    .build();
            
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("geoDataSyncTrigger", "geoGroup")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(syncIntervalMinutes)
                            .repeatForever())
                    .startNow()
                    .build();
            
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Geo data sync job scheduled with interval: {} minutes", syncIntervalMinutes);
            
        } catch (Exception e) {
            log.error("Error scheduling geo data sync job", e);
        }
    }
    
    /**
     * Job для синхронизации геоданных
     */
    @RequiredArgsConstructor
    @Slf4j
    public static class GeoDataSyncJob implements Job {
        
        private final GeoBitrixSyncService geoBitrixSyncService;
        
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                log.info("Starting geo data sync to Bitrix24");
                geoBitrixSyncService.syncGeoDataToBitrix();
                log.info("Geo data sync completed successfully");
            } catch (Exception e) {
                log.error("Error during geo data sync", e);
                throw new JobExecutionException(e);
            }
        }
    }
}
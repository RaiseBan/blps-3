package com.example.blps.service.scheduler;

import com.example.blps.service.geo.GeoBitrixSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(name = "scheduler.master.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class GeoDataSyncScheduler {

    private final Scheduler scheduler;

    @Value("${geo.sync.interval.minutes}")
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

    @Slf4j
    public static class GeoDataSyncJob extends BaseQuartzJob {

        @Override
        protected void executeInternal(JobExecutionContext context) throws Exception {
            try {
                log.info("Starting geo data sync to Bitrix24");

                GeoBitrixSyncService geoBitrixSyncService = applicationContext.getBean(GeoBitrixSyncService.class);
                geoBitrixSyncService.syncGeoDataToBitrix();

                log.info("Geo data sync completed successfully");
            } catch (Exception e) {
                log.error("Error during geo data sync", e);
                throw new JobExecutionException(e);
            }
        }
    }
}
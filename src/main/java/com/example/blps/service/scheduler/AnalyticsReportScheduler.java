package com.example.blps.service.scheduler;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.model.notification.DashboardType;
import com.example.blps.service.notification.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "scheduler.master.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsReportScheduler {

    private final Scheduler scheduler;

    @PostConstruct
    public void initializeJob() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(AnalyticsReportJob.class)
                    .withIdentity("analyticsReportJob", "reportGroup")
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("analyticsReportTrigger", "reportGroup")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(5) // каждые 5 минут
                            .repeatForever())
                    .startNow()
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Analytics report job scheduled with 5 minutes interval");

        } catch (Exception e) {
            log.error("Error scheduling analytics report job", e);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    public static class AnalyticsReportJob extends BaseQuartzJob {

        @Override
        protected void executeInternal(JobExecutionContext context) {
            log.info("=== AnalyticsReportJob START ===");

            MessageSenderService messageSenderService = applicationContext.getBean(MessageSenderService.class);

            DashboardGenerationRequest request = DashboardGenerationRequest.builder()
                    .type(DashboardType.ANALYTICS_REPORT)
                    .title("Аналитический отчет за неделю")
                    .description("Аналитика эффективности рекламных кампаний за последние 7 дней")
                    .startDate(LocalDate.now().minusDays(7))
                    .endDate(LocalDate.now())
                    .autoPublish(true)
                    .recipientsGroup("ROLE_ANALYST,ROLE_ADMIN")
                    .createdBy("system-scheduler")
                    .build();

            log.info("=== Sending analytics report request: {} ===", request);
            messageSenderService.sendDashboardGenerationRequest(request);
            log.info("=== AnalyticsReportJob END ===");
        }
    }
}
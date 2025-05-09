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
@ConditionalOnProperty(name = "financial.node.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class FinancialReportScheduler {

    private final Scheduler scheduler;

    @PostConstruct
    public void initializeJob() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(FinancialReportJob.class)
                    .withIdentity("financialReportJob", "reportGroup")
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("financialReportTrigger", "reportGroup")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(5) 
                            .repeatForever())
                    .startNow()
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Financial report job scheduled with 5 minutes interval");

        } catch (Exception e) {
            log.error("Error scheduling financial report job", e);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    public static class FinancialReportJob extends BaseQuartzJob {

        @Override
        protected void executeInternal(JobExecutionContext context) {
            log.info("=== FinancialReportJob START ===");

            MessageSenderService messageSenderService = applicationContext.getBean(MessageSenderService.class);

            DashboardGenerationRequest request = DashboardGenerationRequest.builder()
                    .type(DashboardType.FINANCIAL_REPORT)
                    .title("Финансовый отчет за неделю")
                    .description("Финансовые показатели и бюджеты кампаний за последние 7 дней")
                    .startDate(LocalDate.now().minusDays(7))
                    .endDate(LocalDate.now())
                    .autoPublish(true)
                    .recipientsGroup("ROLE_ADMIN,ROLE_CAMPAIGN_MANAGER")
                    .createdBy("system-scheduler")
                    .build();

            log.info("=== Sending financial report request: {} ===", request);
            messageSenderService.sendDashboardGenerationRequest(request);
            log.info("=== FinancialReportJob END ===");
        }
    }
}
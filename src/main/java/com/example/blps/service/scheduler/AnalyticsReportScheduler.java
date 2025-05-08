package com.example.blps.service.scheduler;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.model.notification.DashboardType;
import com.example.blps.service.notification.MessageSenderService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// src/main/java/com/example/blps/service/scheduler/AnalyticsReportScheduler.java
// src/main/java/com/example/blps/service/scheduler/AnalyticsReportScheduler.java
@Component
@ConditionalOnProperty(name = "scheduler.master.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsReportScheduler {

    private final MessageSenderService messageSenderService;

    // Используем fixedDelay вместо fixedRate и добавляем initialDelay
    @Scheduled(initialDelay = 120000, fixedDelay = 120000) // 2 минуты задержка перед первым запуском
    public void generateAnalyticsReport() {
        log.info("=== AnalyticsReportScheduler generateAnalyticsReport() START ===");
        log.info("Master node: Generating scheduled analytics report");

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
        log.info("=== AnalyticsReportScheduler generateAnalyticsReport() END ===");
    }
}
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

@Component
@ConditionalOnProperty(name = "financial.node.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class FinancialReportScheduler {

    private final MessageSenderService messageSenderService;


    // Используем fixedDelay вместо fixedRate и добавляем initialDelay
    @Scheduled(initialDelay = 120000, fixedDelay = 120000) // 2 минуты задержка перед первым запуском
    public void generateFinancialReport() {
        log.info("=== FinancialReportScheduler generateFinancialReport() START ===");
        log.info("Master node: Generating scheduled financial report");

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
        log.info("=== FinancialReportScheduler generateFinancialReport() END ===");
    }
}
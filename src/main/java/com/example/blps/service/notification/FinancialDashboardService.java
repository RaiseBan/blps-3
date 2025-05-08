// src/main/java/com/example/blps/service/notification/FinancialDashboardService.java
package com.example.blps.service.notification;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.model.notification.DashboardType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Сервис для обработки финансовых дашбордов (узел 2)
 */
@Service
@ConditionalOnProperty(name = "financial.node.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class FinancialDashboardService {

    private final SimplifiedDashboardService dashboardService;
    
    // Типы дашбордов, обрабатываемые этим узлом
    private static final List<DashboardType> FINANCIAL_TYPES = Arrays.asList(
        DashboardType.BUDGET_ALLOCATION,
        DashboardType.ROI_ANALYSIS,
        DashboardType.WEEKLY_SUMMARY,
        DashboardType.MONTHLY_REPORT
    );

    @JmsListener(
        destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE,
        containerFactory = "jmsListenerContainerFactory",
        selector = "dashboardType IN ('BUDGET_ALLOCATION', 'ROI_ANALYSIS', 'WEEKLY_SUMMARY', 'MONTHLY_REPORT')"
    )
    public void processFinancialDashboard(DashboardGenerationRequest request) {
        log.info("Financial Node: Processing dashboard request: {}", request);
        
        if (!FINANCIAL_TYPES.contains(request.getType())) {
            log.warn("Unexpected dashboard type {} for financial node", request.getType());
            return;
        }
        
        try {
            // Делегируем обработку основному сервису
            dashboardService.processDashboardRequest(request);
            log.info("Financial Node: Dashboard processed successfully");
        } catch (Exception e) {
            log.error("Financial Node: Error processing dashboard", e);
            throw e;
        }
    }
}
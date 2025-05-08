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
// src/main/java/com/example/blps/service/notification/FinancialDashboardService.java
@Service
@ConditionalOnProperty(name = "financial.node.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class FinancialDashboardService {

    private final SimplifiedDashboardService dashboardService;

    @JmsListener(
            destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE,
            containerFactory = "jmsListenerContainerFactory",
            selector = "dashboardType = 'FINANCIAL_REPORT'"
    )
    public void processFinancialDashboard(DashboardGenerationRequest request) {
        log.info("Financial Node: Processing dashboard request: {}", request);
        dashboardService.processDashboardRequest(request);
    }
}
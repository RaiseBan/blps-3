// src/main/java/com/example/blps/service/notification/FinancialDashboardService.java
package com.example.blps.service.notification;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.model.notification.DashboardType;
import jakarta.jms.Message;
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
// src/main/java/com/example/blps/service/notification/FinancialDashboardService.java
@Service
@ConditionalOnProperty(name = "financial.node.enabled", havingValue = "true")
@Slf4j
public class FinancialDashboardService extends BaseDashboardService {

    public FinancialDashboardService(SimplifiedDashboardService dashboardService) {
        super(dashboardService);
    }

    @JmsListener(
            destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE,
            containerFactory = "jmsListenerContainerFactory",
            selector = "dashboardType = 'FINANCIAL_REPORT'",
            id = "financial-dashboard-listener"
    )
    public void processFinancialDashboard(DashboardGenerationRequest request, Message message) {
        processDashboard(request, message);
    }

    @Override
    protected String getNodeType() {
        return "Financial Node";
    }
}
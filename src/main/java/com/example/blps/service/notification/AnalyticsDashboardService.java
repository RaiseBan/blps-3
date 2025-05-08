// src/main/java/com/example/blps/service/notification/AnalyticsDashboardService.java
package com.example.blps.service.notification;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.model.notification.DashboardType;
import jakarta.annotation.PostConstruct;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Сервис для обработки аналитических дашбордов (узел 1)
 */
// src/main/java/com/example/blps/service/notification/AnalyticsDashboardService.java
// src/main/java/com/example/blps/service/notification/AnalyticsDashboardService.java
@Service
@ConditionalOnProperty(name = "analytics.node.enabled", havingValue = "true")
@Slf4j
public class AnalyticsDashboardService extends BaseDashboardService {

    public AnalyticsDashboardService(SimplifiedDashboardService dashboardService) {
        super(dashboardService);
    }

    @PostConstruct
    public void init() {
        log.info("=== AnalyticsDashboardService created: {} ===", this.hashCode());
    }

    @JmsListener(
            destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE,
            containerFactory = "jmsListenerContainerFactory",
            selector = "dashboardType = 'ANALYTICS_REPORT'",
            id = "analytics-dashboard-listener"
    )
    public void processAnalyticsDashboard(DashboardGenerationRequest request, Message message) {
        processDashboard(request, message);
    }

    @Override
    protected String getNodeType() {
        return "Analytics Node";
    }
}
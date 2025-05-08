// src/main/java/com/example/blps/service/notification/AnalyticsDashboardService.java
package com.example.blps.service.notification;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.model.notification.DashboardType;
import jakarta.annotation.PostConstruct;
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
@Service
@ConditionalOnProperty(name = "analytics.node.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsDashboardService {

    private final SimplifiedDashboardService dashboardService;

    @JmsListener(
            destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE,
            containerFactory = "jmsListenerContainerFactory",
            selector = "dashboardType = 'ANALYTICS_REPORT'"
    )
    public void processAnalyticsDashboard(DashboardGenerationRequest request) {
        log.info("Analytics Node: Processing dashboard request in instance: {}", this.hashCode());
        dashboardService.processDashboardRequest(request);
    }
}
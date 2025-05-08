// src/main/java/com/example/blps/service/notification/AnalyticsDashboardService.java
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
 * Сервис для обработки аналитических дашбордов (узел 1)
 */
@Service
@ConditionalOnProperty(name = "analytics.node.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsDashboardService {

    private final SimplifiedDashboardService dashboardService;
    
    // Типы дашбордов, обрабатываемые этим узлом
    private static final List<DashboardType> ANALYTICS_TYPES = Arrays.asList(
        DashboardType.CAMPAIGN_PERFORMANCE,
        DashboardType.CLICK_RATES,
        DashboardType.CONVERSION_RATES
    );

    @JmsListener(
        destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE,
        containerFactory = "jmsListenerContainerFactory",
        selector = "dashboardType IN ('CAMPAIGN_PERFORMANCE', 'CLICK_RATES', 'CONVERSION_RATES')"
    )
    public void processAnalyticsDashboard(DashboardGenerationRequest request) {
        log.info("Analytics Node: Processing dashboard request: {}", request);
        
        if (!ANALYTICS_TYPES.contains(request.getType())) {
            log.warn("Unexpected dashboard type {} for analytics node", request.getType());
            return;
        }
        
        try {
            // Делегируем обработку основному сервису
            dashboardService.processDashboardRequest(request);
            log.info("Analytics Node: Dashboard processed successfully");
        } catch (Exception e) {
            log.error("Analytics Node: Error processing dashboard", e);
            throw e;
        }
    }
}
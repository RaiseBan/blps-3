package com.example.blps.service.notification;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public abstract class BaseDashboardService {
    
    protected final SimplifiedDashboardService dashboardService;
    private final Set<String> processedMessages = Collections.synchronizedSet(new HashSet<>());
    
    protected BaseDashboardService(SimplifiedDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    protected void processDashboard(DashboardGenerationRequest request, Message message) {
        try {
            String messageId = message.getJMSMessageID();
            
            if (processedMessages.contains(messageId)) {
                log.warn("Message {} already processed, skipping", messageId);
                return;
            }
            
            log.info("{}: Processing dashboard request with JMS ID: {}", getNodeType(), messageId);
            dashboardService.processDashboardRequest(request);
            
            processedMessages.add(messageId);
            
            // Очистка старых сообщений
            if (processedMessages.size() > 1000) {
                processedMessages.clear();
            }
        } catch (JMSException e) {
            log.error("Error processing message", e);
            throw new RuntimeException(e);
        }
    }
    
    protected abstract String getNodeType();
}
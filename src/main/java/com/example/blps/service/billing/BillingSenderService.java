package com.example.blps.service.billing;

import com.example.blps.dto.billing.BillingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки биллинговых запросов в очередь
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingSenderService {
    
    private final JmsTemplate jmsTemplate;
    
    /**
     * Отправляет запрос на генерацию счета в очередь для асинхронной обработки
     */
    public void sendBillingRequest(BillingRequest request) {
        log.info("Sending billing request to queue for campaign: {}", request.getCampaignId());
        try {
            jmsTemplate.convertAndSend(BillingProcessingService.BILLING_PROCESSING_QUEUE, request);
            log.info("Billing request sent successfully");
        } catch (Exception e) {
            log.error("Error sending billing request to queue", e);
            throw new RuntimeException("Failed to send billing request", e);
        }
    }
}
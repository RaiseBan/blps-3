package com.example.blps.controllers;

import com.example.blps.dto.billing.BillingRequest;
import com.example.blps.dto.billing.BillingType;
import com.example.blps.model.billing.BillingData;
import com.example.blps.service.billing.BillingCalculationService;
import com.example.blps.service.billing.BillingProcessingService;
import com.example.blps.service.billing.BillingSenderService;
import com.example.blps.service.integration.Bitrix24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/billing-test")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class BillingTestController {
    
    private final BillingSenderService billingSenderService;
    private final BillingCalculationService billingCalculationService;
    private final Bitrix24Service bitrix24Service;
    
    /**
     * Тестирует расчет биллинга без отправки
     */
    @GetMapping("/calculate/{campaignId}")
    public ResponseEntity<BillingData> testCalculation(@PathVariable Long campaignId) {
        log.info("=== ТЕСТ РАСЧЕТА БИЛЛИНГА ===");
        
        BillingRequest request = BillingRequest.builder()
                .campaignId(campaignId)
                .periodStart(LocalDate.now().minusDays(7))
                .periodEnd(LocalDate.now())
                .requestedBy("test")
                .billingType(BillingType.ON_DEMAND)
                .build();
        
        BillingData result = billingCalculationService.calculateBilling(request);
        log.info("Расчет завершен: {}", result);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Тестирует полный цикл биллинга с отправкой в Bitrix24
     */
    @PostMapping("/full-test/{campaignId}")
    public ResponseEntity<String> testFullCycle(@PathVariable Long campaignId) {
        log.info("=== ПОЛНЫЙ ТЕСТ БИЛЛИНГА ===");
        
        BillingRequest request = BillingRequest.builder()
                .campaignId(campaignId)
                .periodStart(LocalDate.now().minusDays(7))
                .periodEnd(LocalDate.now())
                .requestedBy("test-full")
                .billingType(BillingType.ON_DEMAND)
                .build();
        
        log.info("Отправляем запрос в очередь...");
        billingSenderService.sendBillingRequest(request);
        
        return ResponseEntity.ok("Billing process started. Check logs and Bitrix24.");
    }
    
    /**
     * Тестирует прямое создание счета в Bitrix24
     */
    @PostMapping("/direct-bitrix-test")
    public ResponseEntity<String> testDirectBitrix() {
        log.info("=== ПРЯМОЙ ТЕСТ BITRIX24 ===");
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("fields[TITLE]", "Тестовый счет");
            params.put("fields[OPPORTUNITY]", "1000");
            params.put("fields[CURRENCY_ID]", "RUB");
            params.put("fields[STATUS_ID]", "NEW");
            params.put("fields[COMMENTS]", "Это тестовый счет для проверки интеграции");
            
            log.info("Отправляем тестовый счет в Bitrix24...");
            String result = bitrix24Service.connector.executeMethod("crm.invoice.add", params);
            log.info("Результат: {}", result);
            
            return ResponseEntity.ok("Test invoice created: " + result);
        } catch (Exception e) {
            log.error("Ошибка создания тестового счета", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Проверяет статус очереди JMS
     */
    @GetMapping("/queue-status")
    public ResponseEntity<Map<String, Object>> checkQueueStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("queueName", BillingProcessingService.BILLING_PROCESSING_QUEUE);
        status.put("timestamp", java.time.LocalDateTime.now());
        status.put("active", "JMS is configured");
        
        log.info("Queue status check: {}", status);
        return ResponseEntity.ok(status);
    }
}
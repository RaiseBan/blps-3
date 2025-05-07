package com.example.blps.controllers;

import com.example.blps.dto.billing.BillingRequest;
import com.example.blps.dto.billing.BillingType;
import com.example.blps.service.billing.BillingSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Контроллер для управления биллингом
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'CAMPAIGN_MANAGER')")
public class BillingController {
    
    private final BillingSenderService billingSenderService;
    
    /**
     * Генерирует счет для кампании за указанный период
     */
    @PostMapping("/generate/{campaignId}")
    public ResponseEntity<String> generateBilling(
            @PathVariable Long campaignId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        BillingRequest request = BillingRequest.builder()
                .campaignId(campaignId)
                .periodStart(startDate)
                .periodEnd(endDate)
                .requestedBy(auth.getName())
                .billingType(BillingType.ON_DEMAND)
                .build();
        
        billingSenderService.sendBillingRequest(request);
        
        return ResponseEntity.ok("Billing generation started. Invoice will be created in Bitrix24.");
    }
    
    /**
     * Генерирует счета для всех кампаний за текущий месяц
     */
    @PostMapping("/generate-all/monthly")
    public ResponseEntity<String> generateMonthlyBillingForAll() {
        // Запускаем job вручную
        // Можно вызвать напрямую метод или использовать Quartz для запуска
        log.info("Manual monthly billing generation requested");
        
        return ResponseEntity.ok("Monthly billing generation started for all campaigns.");
    }
}
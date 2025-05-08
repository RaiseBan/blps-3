package com.example.blps.controllers;

import com.example.blps.service.scheduler.BillingScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class SimpleBillingController {
    
    private final BillingScheduler billingScheduler;
    
    /**
     * Запустить генерацию счетов вручную
     */
    @PostMapping("/generate-now")
    public ResponseEntity<String> generateNow() {
        log.info("Ручной запуск генерации счетов");
        billingScheduler.generateBills();
        return ResponseEntity.ok("Генерация счетов запущена");
    }
    
    /**
     * Проверить статус
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("scheduler", "active");
        status.put("interval", "5 minutes");
        status.put("time", java.time.LocalDateTime.now());
        return ResponseEntity.ok(status);
    }
}
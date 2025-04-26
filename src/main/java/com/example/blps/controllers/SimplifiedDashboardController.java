package com.example.blps.controllers;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.service.notification.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления дашбордами
 */
@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
@Slf4j
public class SimplifiedDashboardController {

    private final MessageSenderService messageSenderService;

    /**
     * Создает новый запрос на генерацию дашборда
     *
     * @param request запрос с данными для генерации дашборда
     * @return результат операции
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<String> generateDashboard(@RequestBody DashboardGenerationRequest request) {
        // Устанавливаем информацию о создателе
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        request.setCreatedBy(auth.getName());

        try {
            log.info("Received dashboard generation request: {}", request);

            // Отправляем запрос в очередь для асинхронной обработки
            messageSenderService.sendDashboardGenerationRequest(request);

            return ResponseEntity.ok("Dashboard generation process started successfully. You will be notified when it's ready.");
        } catch (Exception e) {
            log.error("Error starting dashboard generation", e);
            return ResponseEntity.status(500).body("Error starting dashboard generation: " + e.getMessage());
        }
    }

    /**
     * Запускает ручную генерацию еженедельного отчета
     */
    @PostMapping("/generate/weekly")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<String> generateWeeklyDashboard() {
        try {
            // Создаем запрос на генерацию еженедельного дашборда
            DashboardGenerationRequest request = DashboardGenerationRequest.builder()
                    .type(com.example.blps.model.notification.DashboardType.WEEKLY_SUMMARY)
                    .title("Еженедельный отчет (ручной запуск)")
                    .description("Еженедельный отчет, созданный вручную")
                    .startDate(java.time.LocalDate.now().minusDays(7))
                    .endDate(java.time.LocalDate.now())
                    .autoPublish(true)
                    .recipientsGroup("ROLE_ANALYST,ROLE_ADMIN")
                    .createdBy(SecurityContextHolder.getContext().getAuthentication().getName())
                    .build();

            messageSenderService.sendDashboardGenerationRequest(request);

            return ResponseEntity.ok("Weekly dashboard generation started. You will be notified when it's ready.");
        } catch (Exception e) {
            log.error("Error starting weekly dashboard generation", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Запускает ручную генерацию ежемесячного отчета
     */
    @PostMapping("/generate/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<String> generateMonthlyDashboard() {
        try {
            // Определяем предыдущий месяц
            java.time.YearMonth previousMonth = java.time.YearMonth.now().minusMonths(1);

            // Создаем запрос на генерацию ежемесячного дашборда
            DashboardGenerationRequest request = DashboardGenerationRequest.builder()
                    .type(com.example.blps.model.notification.DashboardType.MONTHLY_REPORT)
                    .title("Ежемесячный отчет: " + previousMonth.getMonth() + " " + previousMonth.getYear() + " (ручной запуск)")
                    .description("Ежемесячный отчет, созданный вручную")
                    .startDate(previousMonth.atDay(1))
                    .endDate(previousMonth.atEndOfMonth())
                    .autoPublish(true)
                    .recipientsGroup("ROLE_ANALYST,ROLE_ADMIN,ROLE_CAMPAIGN_MANAGER")
                    .createdBy(SecurityContextHolder.getContext().getAuthentication().getName())
                    .build();

            messageSenderService.sendDashboardGenerationRequest(request);

            return ResponseEntity.ok("Monthly dashboard generation started. You will be notified when it's ready.");
        } catch (Exception e) {
            log.error("Error starting monthly dashboard generation", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
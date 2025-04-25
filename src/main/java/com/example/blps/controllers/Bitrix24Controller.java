package com.example.blps.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.model.dataEntity.TheirCampaign;
import com.example.blps.service.data.OurCampaignService;
import com.example.blps.service.data.ReportService;
import com.example.blps.service.data.TheirCampaignService;
import com.example.blps.service.integration.Bitrix24Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Контроллер для управления интеграцией с Битрикс24.
 * Предоставляет эндпоинты для синхронизации данных между системой и Битрикс24.
 */
@RestController
@RequestMapping("/api/bitrix24")
@RequiredArgsConstructor
@Slf4j
public class Bitrix24Controller {

    private final Bitrix24Service bitrixService;
    private final OurCampaignService ourCampaignService;
    private final TheirCampaignService theirCampaignService;
    private final ReportService reportService;

    /**
     * Создает задачу в Битрикс24 для выбранной кампании.
     *
     * @param id ID кампании
     * @return ID созданной задачи в Битрикс24
     */
    @PostMapping("/our-campaigns/{id}/create-task")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAMPAIGN_MANAGER')")
    public ResponseEntity<String> createCampaignTask(@PathVariable Long id) {
        OurCampaignDTO campaign = ourCampaignService.getCampaignById(id);
        String taskId = bitrixService.createCampaignTask(campaign);

        return ResponseEntity.ok("Задача успешно создана в Битрикс24 с ID: " + taskId);
    }

    /**
     * Создает лид в Битрикс24 CRM для партнерской кампании.
     *
     * @param id ID партнерской кампании
     * @return ID созданного лида в Битрикс24
     */
    @PostMapping("/their-campaigns/{id}/create-lead")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAMPAIGN_MANAGER')")
    public ResponseEntity<String> createPartnerLead(@PathVariable Long id) {
        TheirCampaign campaign = theirCampaignService.getCampaignById(id);
        String leadId = bitrixService.createPartnerLead(campaign);

        return ResponseEntity.ok("Лид успешно создан в Битрикс24 с ID: " + leadId);
    }

    /**
     * Отправляет отчеты о всех кампаниях в Битрикс24.
     *
     * @return результат отправки
     */
    @PostMapping("/reports/sync")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYST')")
    public ResponseEntity<String> syncCampaignReports() {
        List<CampaignReportDTO> reports = reportService.getCampaignsReportData();
        String result = bitrixService.sendCampaignReports(reports);

        return ResponseEntity.ok("Отчеты успешно отправлены в Битрикс24: " + result);
    }

    /**
     * Получает статус интеграции с Битрикс24.
     *
     * @return статус соединения
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getStatus() {
        try {
            // Простой запрос для проверки подключения
            return ResponseEntity.ok("Соединение с Битрикс24 активно");
        } catch (Exception e) {
            log.error("Error checking Bitrix24 connection", e);
            return ResponseEntity.status(500).body("Ошибка соединения с Битрикс24: " + e.getMessage());
        }
    }

    @GetMapping("/test-connection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testConnection() {
        try {
            // Простой запрос для проверки системной информации Битрикс24
            Map<String, Object> params = new HashMap<>();
            String result = bitrixService.connector.executeMethod("app.info", params);
            return ResponseEntity.ok("Соединение успешно! Ответ: " + result);
        } catch (Exception e) {
            log.error("Error testing Bitrix24 connection", e);
            return ResponseEntity.status(500).body("Ошибка соединения: " + e.getMessage());
        }
    }
}
package com.example.blps.service.integration;

import jakarta.resource.ResourceException;

import org.springframework.stereotype.Service;

import com.example.blps.connector.Bitrix24Connector;
import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.model.dataEntity.TheirCampaign;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с Битрикс24 через JCA коннектор.
 * Предоставляет методы для создания задач, лидов и других сущностей в Битрикс24 CRM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Bitrix24Service {

    public final Bitrix24Connector connector;
    private final ObjectMapper objectMapper;

    /**
     * Создает задачу в Битрикс24 на основе кампании.
     *
     * @param campaign данные кампании
     * @return ID созданной задачи
     */
    public String createCampaignTask(OurCampaignDTO campaign) {
        try {
            String title = "Обработать кампанию: " + campaign.getCampaignName();
            String description = String.format(
                    "Кампания: %s\nБюджет: %s\nСсылка: %s\nURL размещения: %s",
                    campaign.getCampaignName(),
                    campaign.getBudget(),
                    campaign.getReferralLink(),
                    campaign.getPlacementUrl()
            );

            return connector.createTask(title, description, "1"); // ID ответственного
        } catch (ResourceException e) {
            log.error("Error creating task in Bitrix24", e);
            throw new RuntimeException("Failed to create task in Bitrix24", e);
        }
    }

    /**
     * Создает лид в Битрикс24 CRM для партнерской кампании.
     *
     * @param campaign партнерская кампания
     * @return ID созданного лида
     */
    public String createPartnerLead(TheirCampaign campaign) {
        try {
            String title = "Партнер: " + campaign.getPartnerName();
            String description = String.format(
                    "Период: %s - %s\nСтатус: %s",
                    campaign.getStartDate(),
                    campaign.getEndDate(),
                    campaign.getStatus()
            );

            return connector.createLead(title, campaign.getPartnerName(),
                    "partner@example.com", "+71234567890");
        } catch (ResourceException e) {
            log.error("Error creating lead in Bitrix24", e);
            throw new RuntimeException("Failed to create lead in Bitrix24", e);
        }
    }

    /**
     * Отправляет отчет о результатах кампаний в Битрикс24.
     *
     * @param reports список отчетов по кампаниям
     * @return результат операции
     */
    public String sendCampaignReports(List<CampaignReportDTO> reports) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("TITLE", "Отчет по рекламным кампаниям");

            StringBuilder reportText = new StringBuilder("Данные по кампаниям:\n\n");

            for (CampaignReportDTO report : reports) {
                reportText.append(String.format(
                        "- %s: Бюджет: %s, Клики: %d, CTR: %s%%, Конверсия: %s%%, ROI: %s%%\n",
                        report.getCampaignName(),
                        report.getBudget(),
                        report.getClickCount(),
                        report.getCtr(),
                        report.getConversionRate(),
                        report.getRoi()
                ));
            }

            params.put("DESCRIPTION", reportText.toString());

            return connector.executeMethod("disk.storage.uploadfile", params);
        } catch (ResourceException e) {
            log.error("Error sending campaign reports to Bitrix24", e);
            throw new RuntimeException("Failed to send campaign reports to Bitrix24", e);
        }
    }

    /**
     * Синхронизирует оптимизацию бюджета с Битрикс24.
     *
     * @param campaignId ID кампании
     * @param campaignName название кампании
     * @param oldBudget старый бюджет
     * @param newBudget новый бюджет
     * @return результат операции
     */
    public String syncBudgetOptimization(Long campaignId, String campaignName,
                                         java.math.BigDecimal oldBudget,
                                         java.math.BigDecimal newBudget) {
        try {
            Map<String, Object> params = new HashMap<>();

            String title = "Оптимизация бюджета: " + campaignName;
            String description = String.format(
                    "Проведена автоматическая оптимизация бюджета для кампании %s (ID: %d).\n" +
                            "Старый бюджет: %s\n" +
                            "Новый бюджет: %s\n" +
                            "Изменение: %s%%",
                    campaignName,
                    campaignId,
                    oldBudget,
                    newBudget,
                    calculatePercentChange(oldBudget, newBudget)
            );

            return connector.createTask(title, description, "1"); // ID ответственного
        } catch (ResourceException e) {
            log.error("Error syncing budget optimization with Bitrix24", e);
            throw new RuntimeException("Failed to sync budget optimization with Bitrix24", e);
        }
    }

    /**
     * Рассчитывает процентное изменение между двумя значениями.
     */
    private String calculatePercentChange(java.math.BigDecimal oldValue, java.math.BigDecimal newValue) {
        if (oldValue.equals(java.math.BigDecimal.ZERO)) {
            return "∞"; // Избегаем деления на ноль
        }

        java.math.BigDecimal change = newValue.subtract(oldValue)
                .multiply(new java.math.BigDecimal("100"))
                .divide(oldValue, 2, java.math.RoundingMode.HALF_UP);

        return change.toString();
    }
}
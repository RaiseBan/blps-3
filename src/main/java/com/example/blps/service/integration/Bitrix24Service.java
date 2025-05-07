package com.example.blps.service.integration;

import com.example.blps.dto.notification.ChartData;
import jakarta.resource.ResourceException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.blps.connector.Bitrix24Connector;
import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.model.dataEntity.TheirCampaign;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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

    @Value("${dashboards.diskfolder.id}")
    private int folderId;

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

            return createTask(title, description, "1"); // ID ответственного
        } catch (ResourceException e) {
            log.error("Error creating task in Bitrix24", e);
            throw new RuntimeException("Failed to create task in Bitrix24", e);
        }
    }

    /**
     * Создает задачу в Битрикс24.
     *
     * @param title название задачи
     * @param description описание задачи
     * @param responsibleId ID ответственного сотрудника
     * @return ID созданной задачи
     * @throws ResourceException если произошла ошибка при создании задачи
     */
    public String createTask(String title, String description, String responsibleId) throws ResourceException {
        Map<String, Object> params = new HashMap<>();
        params.put("fields[TITLE]", title);
        params.put("fields[DESCRIPTION]", description);
        params.put("fields[RESPONSIBLE_ID]", responsibleId);

        return connector.executeMethod("tasks.task.add", params);
    }

    public String uploadFile(String fileName, int folderId, String fileContent) throws ResourceException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", folderId);
        params.put("fileContent", fileContent);
        params.put("data[NAME]", fileName);
        params.put("generateUniqueName", true);

        return connector.executeMethod("disk.folder.uploadfile", params);
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

            return createTask(title, description, "1"); // ID ответственного
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

    /**
     * Создает задачу в Битрикс24 с приложенной визуализацией дашборда.
     *
     * @param title заголовок задачи
     * @param description описание задачи
     * @param chartData данные для визуализации
     * @param responsibleId ID ответственного сотрудника
     * @return ID созданной задачи
     * @throws ResourceException если произошла ошибка при создании задачи
     */
    public String createDashboardTask(String title, String description, ChartData chartData, String responsibleId) throws ResourceException {
        try {
            // Преобразуем данные диаграммы в текстовое представление
            String chartDataJson = objectMapper.writeValueAsString(chartData);

            // Подготавливаем полное описание, включающее данные диаграммы
            StringBuilder fullDescription = new StringBuilder(description);
            fullDescription.append("\n\n--- Данные диаграммы ---\n");
            fullDescription.append("Тип: ").append(chartData.getChartType()).append("\n");
            fullDescription.append("Заголовок: ").append(chartData.getTitle()).append("\n");
            if (chartData.getXAxisLabel() != null) {
                fullDescription.append("Ось X: ").append(chartData.getXAxisLabel()).append("\n");
            }
            if (chartData.getYAxisLabel() != null) {
                fullDescription.append("Ось Y: ").append(chartData.getYAxisLabel()).append("\n");
            }

            // Добавляем текстовое представление данных для использования в Bitrix24
            fullDescription.append("\nДанные диаграммы:\n");
            fullDescription.append(chartDataJson);

            // Создаем задачу с расширенным описанием
            log.info("Creating Bitrix24 dashboard task: {}", title);
            return createTask(title, fullDescription.toString(), responsibleId);

        } catch (Exception e) {
            log.error("Error creating dashboard task in Bitrix24", e);
            throw new ResourceException("Failed to create dashboard task in Bitrix24", e);
        }
    }

    /**
     * Создает сообщение в живой ленте Битрикс24 с данными дашборда.
     *
     * @param title заголовок сообщения
     * @param message текст сообщения
     * @param chartData данные для визуализации
     * @return ID созданного сообщения
     * @throws ResourceException если произошла ошибка при создании сообщения
     */
    public String createLiveFeedMessage(String title, String message, ChartData chartData) throws ResourceException {
        try {
            // Преобразуем данные диаграммы в текстовое представление
            String chartDataJson = objectMapper.writeValueAsString(chartData);

            // Подготавливаем полное сообщение
            StringBuilder fullMessage = new StringBuilder();
            fullMessage.append("<b>").append(title).append("</b><br><br>");
            fullMessage.append(message.replace("\n", "<br>"));
            fullMessage.append("<br><br><b>Данные диаграммы:</b><br>");
            fullMessage.append("<pre>").append(chartDataJson).append("</pre>");

            // Параметры для создания сообщения в живой ленте
            Map<String, Object> params = new HashMap<>();
            params.put("POST_TITLE", title);
            params.put("MESSAGE", fullMessage.toString());
            params.put("DEST", "[\"UA\"]"); // Отправка всем пользователям

            return connector.executeMethod("socialnetwork.livefeed.post.add", params);

        } catch (Exception e) {
            log.error("Error creating live feed message in Bitrix24", e);
            throw new ResourceException("Failed to create live feed message in Bitrix24", e);
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

            // Отправляем данные в Битрикс24
            return connector.executeMethod("disk.storage.uploadfile", params);
        } catch (ResourceException e) {
            log.error("Error sending campaign reports to Bitrix24", e);
            throw new RuntimeException("Failed to send campaign reports to Bitrix24", e);
        }
    }

    /**
     * Проверяет состояние подключения к Битрикс24.
     *
     * @return информация о состоянии подключения
     * @throws ResourceException если произошла ошибка при проверке подключения
     */
    public String checkConnection() throws ResourceException {
        Map<String, Object> params = new HashMap<>();
        return connector.executeMethod("app.info", params);
    }

    public String createTaskWithImage(String title, String description, String responsibleId,
                                      String imageBase64, String fileName) throws ResourceException {
        try {
            // Сначала создаем задачу
            Map<String, Object> resultMap = objectMapper.readValue(createTask(title, description, responsibleId), Map.class);

            Map<String, Object> result = (Map<String, Object>) resultMap.get("result");
            Map<String, Object> task = (Map<String, Object>) result.get("task");
            String taskId = (String) task.get("id");
            log.info("Task created with ID: {}", taskId);

            Map<String, Object> fileMap = objectMapper.readValue(uploadFile(fileName, folderId, imageBase64), Map.class);
            Map<String, Object> fileResult = (Map<String, Object>) fileMap.get("result");
            Integer fileId = (Integer) fileResult.get("ID");

            log.info("File loaded with ID: {}", fileId);


            Map<String, Object> params = new HashMap<>();
            params.put("taskId", taskId);
            params.put("fileId", fileId);

            return connector.executeMethod("tasks.task.files.attach", params);
        } catch (Exception e) {
            log.error("Error creating task with image", e);
            throw new ResourceException("Failed to create task with image", e);
        }
    }
}
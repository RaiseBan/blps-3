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

@Service
@RequiredArgsConstructor
@Slf4j
public class Bitrix24Service {

    public final Bitrix24Connector connector;
    private final ObjectMapper objectMapper;

    @Value("${dashboards.diskfolder.id}")
    private int folderId;

    public String executeAbstractMethod(String method, Map<String, Object> params) throws ResourceException {
        return connector.executeMethod(method, params);
    }

    public void sendSystemNotificationToBitrix(String responsibleId, String message) throws ResourceException {
        log.info("Sending  notification to user: {}", responsibleId);
        Map<String, Object> notificationParams = new HashMap<>();
        notificationParams.put("USER_ID", responsibleId);
        notificationParams.put("MESSAGE", message);
        connector.executeMethod("im.notify.system.add", notificationParams);
    }

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

            return createTask(title, description, "1"); 
        } catch (ResourceException e) {
            log.error("Error syncing budget optimization with Bitrix24", e);
            throw new RuntimeException("Failed to sync budget optimization with Bitrix24", e);
        }
    }

    private String calculatePercentChange(java.math.BigDecimal oldValue, java.math.BigDecimal newValue) {
        if (oldValue.equals(java.math.BigDecimal.ZERO)) {
            return "∞"; 
        }

        java.math.BigDecimal change = newValue.subtract(oldValue)
                .multiply(new java.math.BigDecimal("100"))
                .divide(oldValue, 2, java.math.RoundingMode.HALF_UP);

        return change.toString();
    }

    public String createLiveFeedMessage(String title, String message, ChartData chartData) throws ResourceException {
        try {
            
            String chartDataJson = objectMapper.writeValueAsString(chartData);

            StringBuilder fullMessage = new StringBuilder();
            fullMessage.append("<b>").append(title).append("</b><br><br>");
            fullMessage.append(message.replace("\n", "<br>"));
            fullMessage.append("<br><br><b>Данные диаграммы:</b><br>");
            fullMessage.append("<pre>").append(chartDataJson).append("</pre>");

            Map<String, Object> params = new HashMap<>();
            params.put("POST_TITLE", title);
            params.put("MESSAGE", fullMessage.toString());
            params.put("DEST", "[\"UA\"]"); 

            return connector.executeMethod("socialnetwork.livefeed.post.add", params);

        } catch (Exception e) {
            log.error("Error creating live feed message in Bitrix24", e);
            throw new ResourceException("Failed to create live feed message in Bitrix24", e);
        }
    }

    public String checkConnection() throws ResourceException {
        Map<String, Object> params = new HashMap<>();
        return connector.executeMethod("app.info", params);
    }

    public String createTaskWithImage(String title, String description, String responsibleId,
                                      String imageBase64, String fileName) throws ResourceException {
        try {
            
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
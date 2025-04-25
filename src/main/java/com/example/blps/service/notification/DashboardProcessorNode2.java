package com.example.blps.service.notification;

import com.example.blps.dto.notification.ChartData;
import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.Dashboard;
import com.example.blps.model.notification.DashboardType;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.service.integration.Bitrix24Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для обработки запросов на генерацию дашбордов на втором узле
 * Использует профиль "node2" для запуска только на втором узле
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile("node2")
public class DashboardProcessorNode2 {

    private final ObjectMapper objectMapper;
    private final MessageSenderService messageSenderService;
    private final Bitrix24Service bitrix24Service;
    private final PlatformTransactionManager transactionManager;
    
    @Value("${node.id:node2}")
    private String nodeId;
    
    /**
     * Обрабатывает запросы на генерацию дашбордов из очереди
     * 
     * @param request запрос на генерацию дашборда
     */
    @JmsListener(destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE)
    public void processDashboardRequest(DashboardGenerationRequest request) {
        log.info("Node {} processing dashboard generation request: {}", nodeId, request);
        
        // Создаем транзакцию для распределенной обработки
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setName("dashboardProcessingTransaction");
        
        TransactionStatus status = transactionManager.getTransaction(def);
        
        try {
            // Генерируем данные графика на основе запроса
            ChartData chartData = generateChartData(request);
            
            // Формируем данные дашборда
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("title", request.getTitle());
            dashboardData.put("type", request.getType().toString());
            dashboardData.put("chartData", chartData.getData());
            dashboardData.put("chartConfig", chartData.getOptions());
            dashboardData.put("createdAt", LocalDateTime.now().toString());
            dashboardData.put("nodeId", nodeId);
            
            // Отправляем данные дашборда обратно в основной узел для сохранения
            messageSenderService.sendMessage("dashboard.save.queue", dashboardData);
            
            // Отправляем уведомление о создании дашборда через Bitrix24
            sendBitrix24Notification(request, chartData);
            
            log.info("Dashboard processing completed on node {}", nodeId);
            
            // Коммитим транзакцию
            transactionManager.commit(status);
            
            // Отправляем уведомление в очередь
            sendCompletionNotification(request);
            
        } catch (Exception e) {
            // Откатываем транзакцию в случае ошибки
            transactionManager.rollback(status);
            log.error("Error processing dashboard on node " + nodeId, e);
            
            // Отправляем уведомление об ошибке
            sendErrorNotification(request, e);
        }
    }
    
    /**
     * Генерирует данные графика в зависимости от типа дашборда
     */
    private ChartData generateChartData(DashboardGenerationRequest request) {
        log.info("Generating chart data for dashboard type: {}", request.getType());
        
        // Здесь должна быть логика генерации данных в зависимости от типа
        // Для демонстрации просто создаем фиктивные данные
        
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> options = new HashMap<>();
        
        // Фиктивные данные для различных типов дашбордов
        if (request.getType() == DashboardType.WEEKLY_SUMMARY) {
            data.put("labels", List.of("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"));
            data.put("values", List.of(25, 30, 45, 60, 75, 65, 40));
            options.put("responsive", true);
            
            return ChartData.builder()
                    .chartType("line")
                    .title("Еженедельная активность")
                    .xAxisLabel("День недели")
                    .yAxisLabel("Клики")
                    .data(data)
                    .options(options)
                    .build();
        } else if (request.getType() == DashboardType.MONTHLY_REPORT) {
            data.put("labels", List.of("Неделя 1", "Неделя 2", "Неделя 3", "Неделя 4"));
            
            Map<String, List<Integer>> datasets = new HashMap<>();
            datasets.put("clicks", List.of(120, 150, 180, 210));
            datasets.put("conversions", List.of(10, 15, 20, 25));
            
            data.put("datasets", datasets);
            options.put("responsive", true);
            
            return ChartData.builder()
                    .chartType("bar")
                    .title("Ежемесячный отчет")
                    .xAxisLabel("Недели")
                    .yAxisLabel("Значения")
                    .data(data)
                    .options(options)
                    .build();
        } else {
            // Для других типов дашбордов
            data.put("labels", List.of("Категория 1", "Категория 2", "Категория 3"));
            data.put("values", List.of(30, 50, 20));
            options.put("responsive", true);
            
            return ChartData.builder()
                    .chartType("pie")
                    .title("Общая статистика")
                    .data(data)
                    .options(options)
                    .build();
        }
    }
    
    /**
     * Отправляет уведомление о создании дашборда в Bitrix24
     */
    private void sendBitrix24Notification(DashboardGenerationRequest request, ChartData chartData) {
        try {
            String title = "Создан новый дашборд: " + request.getTitle();
            String description = String.format(
                    "Тип: %s\nОписание: %s\nДата создания: %s\nОбработано узлом: %s",
                    request.getType(),
                    request.getDescription(),
                    LocalDateTime.now(),
                    nodeId
            );
            
            bitrix24Service.createTask(title, description, "1");
            log.info("Bitrix24 notification sent for dashboard: {}", request.getTitle());
        } catch (Exception e) {
            log.error("Error sending Bitrix24 notification", e);
        }
    }
    
    /**
     * Отправляет уведомление о завершении обработки
     */
    private void sendCompletionNotification(DashboardGenerationRequest request) {
        NotificationMessage notification = NotificationMessage.builder()
                .title("Дашборд создан: " + request.getTitle())
                .message("Дашборд успешно создан узлом " + nodeId)
                .type(NotificationType.DASHBOARD_CREATED)
                .recipient(request.getRecipientsGroup())
                .build();
        
        messageSenderService.sendNotification(notification);
    }
    
    /**
     * Отправляет уведомление об ошибке
     */
    private void sendErrorNotification(DashboardGenerationRequest request, Exception e) {
        NotificationMessage notification = NotificationMessage.builder()
                .title("Ошибка создания дашборда: " + request.getTitle())
                .message("Произошла ошибка при создании дашборда на узле " + nodeId + ": " + e.getMessage())
                .type(NotificationType.SYSTEM_ALERT)
                .recipient("ROLE_ADMIN")
                .build();
        
        messageSenderService.sendNotification(notification);
    }
}
package com.example.blps.service.notification;

import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.dto.notification.ChartData;
import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.service.data.ReportService;
import com.example.blps.service.integration.Bitrix24Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.resource.ResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Упрощенный сервис для генерации дашбордов.
 * Получает запросы из очереди, генерирует диаграммы и отправляет их в Bitrix24.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimplifiedDashboardService {

    private final ReportService reportService;
    private final Bitrix24Service bitrix24Service;
    private final MessageSenderService messageSenderService;
    private final ObjectMapper objectMapper;

    /**
     * Обрабатывает запросы на генерацию дашбордов из очереди
     */
    @JmsListener(destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE)
    public void processDashboardRequest(DashboardGenerationRequest request) {
        log.info("Processing dashboard generation request: {}", request);

        try {
            // 1. Генерируем данные диаграммы
            ChartData chartData = generateChartData(request);

            // 2. Отправляем в Bitrix24
            sendToBitrix24(request, chartData);

            // 3. Отправляем уведомление об успешной обработке
            sendSuccessNotification(request);

            log.info("Dashboard generation completed successfully");
        } catch (Exception e) {
            log.error("Error generating dashboard", e);
            sendErrorNotification(request, e);
        }
    }

    /**
     * Генерирует данные для диаграммы в зависимости от типа дашборда
     */
    private ChartData generateChartData(DashboardGenerationRequest request) {
        log.info("Generating chart data for type: {}", request.getType());

        switch (request.getType()) {
            case CAMPAIGN_PERFORMANCE:
                return generateCampaignPerformanceChart();
            case WEEKLY_SUMMARY:
                return generateWeeklySummaryChart();
            case MONTHLY_REPORT:
                return generateMonthlyReportChart();
            // Добавьте другие типы по необходимости
            default:
                throw new IllegalArgumentException("Unsupported dashboard type: " + request.getType());
        }
    }

    /**
     * Отправляет данные дашборда в Bitrix24
     */
    private void sendToBitrix24(DashboardGenerationRequest request, ChartData chartData) {
        try {
            String title = "Дашборд: " + request.getTitle();

            // Формируем описание для задачи
            StringBuilder description = new StringBuilder();
            description.append("Тип: ").append(request.getType()).append("\n");
            description.append("Описание: ").append(request.getDescription()).append("\n");
            description.append("Период: ").append(request.getStartDate()).append(" - ").append(request.getEndDate()).append("\n");
            description.append("Создан: ").append(LocalDateTime.now()).append("\n\n");

            // Добавляем текстовое представление данных
            description.append("Данные диаграммы:\n");
            try {
                description.append(objectMapper.writeValueAsString(chartData.getData())).append("\n\n");
            } catch (Exception e) {
                description.append("[Ошибка сериализации данных]").append("\n\n");
            }

            // Отправляем задачу в Bitrix24
            String taskId = bitrix24Service.createTask(title, description.toString(), "1");
            log.info("Dashboard sent to Bitrix24 as task with ID: {}", taskId);
        } catch (ResourceException e) {
            log.error("Error sending dashboard to Bitrix24", e);
            throw new RuntimeException("Failed to send dashboard to Bitrix24", e);
        }
    }

    /**
     * Отправляет уведомление об успешной обработке
     */
    private void sendSuccessNotification(DashboardGenerationRequest request) {
        NotificationMessage notification = NotificationMessage.builder()
                .title("Дашборд создан: " + request.getTitle())
                .message("Дашборд успешно создан и отправлен в Bitrix24")
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
                .message("Произошла ошибка при создании дашборда: " + e.getMessage())
                .type(NotificationType.SYSTEM_ALERT)
                .recipient("ROLE_ADMIN")
                .build();

        messageSenderService.sendNotification(notification);
    }

    // Методы генерации различных типов диаграмм

    private ChartData generateCampaignPerformanceChart() {
        List<CampaignReportDTO> reports = reportService.getCampaignsReportData();

        Map<String, Object> data = new HashMap<>();
        data.put("labels", reports.stream().map(CampaignReportDTO::getCampaignName).collect(Collectors.toList()));

        Map<String, List<?>> datasets = new HashMap<>();
        datasets.put("clicks", reports.stream().map(CampaignReportDTO::getClickCount).collect(Collectors.toList()));
        datasets.put("ctr", reports.stream().map(CampaignReportDTO::getCtr).collect(Collectors.toList()));
        datasets.put("conversionRate", reports.stream().map(CampaignReportDTO::getConversionRate).collect(Collectors.toList()));
        datasets.put("roi", reports.stream().map(CampaignReportDTO::getRoi).collect(Collectors.toList()));

        data.put("datasets", datasets);

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("title", Map.of("display", true, "text", "Эффективность кампаний"));

        return ChartData.builder()
                .chartType("bar")
                .title("Эффективность кампаний")
                .xAxisLabel("Кампании")
                .yAxisLabel("Значения")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateWeeklySummaryChart() {
        // Пример создания фиктивных данных для недельного отчета
        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"));
        data.put("values", List.of(25, 30, 45, 60, 75, 65, 40));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("title", Map.of("display", true, "text", "Еженедельная активность"));

        return ChartData.builder()
                .chartType("line")
                .title("Еженедельная активность")
                .xAxisLabel("День недели")
                .yAxisLabel("Клики")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateMonthlyReportChart() {
        // Пример создания фиктивных данных для месячного отчета
        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("Неделя 1", "Неделя 2", "Неделя 3", "Неделя 4"));

        Map<String, List<Integer>> datasets = new HashMap<>();
        datasets.put("clicks", List.of(120, 150, 180, 210));
        datasets.put("conversions", List.of(10, 15, 20, 25));

        data.put("datasets", datasets);

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("title", Map.of("display", true, "text", "Ежемесячный отчет"));

        return ChartData.builder()
                .chartType("bar")
                .title("Ежемесячный отчет")
                .xAxisLabel("Недели")
                .yAxisLabel("Значения")
                .data(data)
                .options(options)
                .build();
    }
}
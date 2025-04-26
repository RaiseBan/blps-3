package com.example.blps.service.notification;

import com.example.blps.dto.notification.ChartData;
import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.Dashboard;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.repository.data.MetricRepository;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.service.data.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardGeneratorService {

    private final DashboardSaveService dashboardSaveService;
    private final OurCampaignRepository campaignRepository;
    private final MetricRepository metricRepository;
    private final ReportService reportService;
    private final MessageSenderService messageSenderService;
    private final ObjectMapper objectMapper;

    /**
     * Обрабатывает запросы на генерацию дашбордов из очереди
     *
     * @param request запрос на генерацию дашборда
     */
    @JmsListener(destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE)
    @Transactional
    public void generateDashboard(DashboardGenerationRequest request) {
        log.info("Received dashboard generation request: {}", request);

        try {
            // Генерируем данные для дашборда в зависимости от типа
            ChartData chartData = generateChartData(request);

            // Создаем дашборд
            Dashboard dashboard = new Dashboard();
            dashboard.setTitle(request.getTitle());
            dashboard.setDescription(request.getDescription());
            dashboard.setType(request.getType());

            try {
                dashboard.setChartData(objectMapper.writeValueAsString(chartData.getData()));
                dashboard.setChartConfig(objectMapper.writeValueAsString(chartData.getOptions()));
            } catch (Exception e) {
                log.error("Error serializing chart data", e);
                dashboard.setChartData("{}");
                dashboard.setChartConfig("{}");
            }

            dashboard.setCreatedAt(LocalDateTime.now());
            dashboard.setIsPublished(request.getAutoPublish() != null && request.getAutoPublish());

            // Логируем дашборд перед сохранением
            log.info("Attempting to save dashboard: {}, type: {}", dashboard.getTitle(), dashboard.getType());

            // Используем DashboardSaveService для сохранения, предварительно проверив сущность
            Dashboard savedDashboard = dashboardSaveService.saveDashboardEntity(dashboard);

            if (savedDashboard != null && savedDashboard.getId() != null) {
                log.info("Dashboard saved successfully with ID: {}", savedDashboard.getId());

                // Отправляем уведомление о создании дашборда
                if (request.getRecipientsGroup() != null && !request.getRecipientsGroup().isEmpty()) {
                    sendDashboardNotification(savedDashboard, request.getRecipientsGroup());
                }
            } else {
                log.error("Failed to save dashboard, returned entity is null or has no ID");
            }
        } catch (Exception e) {
            log.error("Error generating dashboard", e);
        }
    }

    /**
     * Отправляет уведомление о создании дашборда
     */
    private void sendDashboardNotification(Dashboard dashboard, String recipientsGroup) {
        NotificationMessage notification = NotificationMessage.builder()
                .title("Новый дашборд создан: " + dashboard.getTitle())
                .message("Создан новый дашборд: " + dashboard.getDescription())
                .type(NotificationType.DASHBOARD_CREATED)
                .recipient(recipientsGroup)
                .relatedEntityId(dashboard.getId())
                .build();

        messageSenderService.sendNotification(notification);
    }

    // Остальные методы остаются без изменений

    // Метод для генерации данных для дашборда
    private ChartData generateChartData(DashboardGenerationRequest request) {
        switch (request.getType()) {
            case CAMPAIGN_PERFORMANCE:
                return generateCampaignPerformanceChart();
            case BUDGET_ALLOCATION:
                return generateBudgetAllocationChart();
            case ROI_ANALYSIS:
                return generateRoiAnalysisChart();
            case CLICK_RATES:
                return generateClickRatesChart();
            case CONVERSION_RATES:
                return generateConversionRatesChart();
            case WEEKLY_SUMMARY:
                return generateWeeklySummaryChart();
            case MONTHLY_REPORT:
                return generateMonthlyReportChart();
            default:
                throw new IllegalArgumentException("Unsupported dashboard type: " + request.getType());
        }
    }

    // Методы для генерации различных типов дашбордов
    private ChartData generateCampaignPerformanceChart() {
        // Реализация метода остается прежней
        // Для краткости код метода опущен
        var campaignReports = reportService.getCampaignsReportData();

        Map<String, Object> data = new HashMap<>();
        data.put("labels", campaignReports.stream().map(r -> r.getCampaignName()).collect(Collectors.toList()));

        Map<String, Object> datasets = new HashMap<>();
        datasets.put("clicks", campaignReports.stream().map(r -> r.getClickCount()).collect(Collectors.toList()));
        datasets.put("ctr", campaignReports.stream().map(r -> r.getCtr()).collect(Collectors.toList()));
        datasets.put("conversionRate", campaignReports.stream().map(r -> r.getConversionRate()).collect(Collectors.toList()));
        datasets.put("roi", campaignReports.stream().map(r -> r.getRoi()).collect(Collectors.toList()));

        data.put("datasets", datasets);

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("maintainAspectRatio", true);
        options.put("scales", Map.of(
                "y", Map.of("beginAtZero", true)
        ));

        return ChartData.builder()
                .chartType("bar")
                .title("Эффективность кампаний")
                .xAxisLabel("Кампании")
                .yAxisLabel("Значения")
                .data(data)
                .options(options)
                .build();
    }

    // Остальные методы генерации графиков без изменений...
    private ChartData generateBudgetAllocationChart() {
        // Фиктивная реализация для краткости
        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("Campaign 1", "Campaign 2", "Campaign 3"));
        data.put("values", List.of(300, 500, 200));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);

        return ChartData.builder()
                .chartType("pie")
                .title("Budget Allocation")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateRoiAnalysisChart() {
        // Фиктивная реализация для краткости
        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("Campaign 1", "Campaign 2", "Campaign 3"));
        data.put("values", List.of(15, 25, -5));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);

        return ChartData.builder()
                .chartType("bar")
                .title("ROI Analysis")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateClickRatesChart() {
        // Фиктивная реализация для краткости
        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("Jan", "Feb", "Mar", "Apr", "May"));
        data.put("values", List.of(120, 150, 180, 200, 220));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);

        return ChartData.builder()
                .chartType("line")
                .title("Click Rates")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateConversionRatesChart() {
        // Фиктивная реализация для краткости
        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("Jan", "Feb", "Mar", "Apr", "May"));
        data.put("values", List.of(2.5, 3.0, 3.5, 4.0, 4.5));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);

        return ChartData.builder()
                .chartType("line")
                .title("Conversion Rates")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateWeeklySummaryChart() {
        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"));
        data.put("values", List.of(25, 30, 45, 60, 75, 65, 40));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);

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
        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("Янв", "Фев", "Мар", "Апр", "Май", "Июн"));

        Map<String, List<Integer>> datasets = new HashMap<>();
        datasets.put("clicks", List.of(120, 150, 180, 210, 250, 300));
        datasets.put("conversions", List.of(10, 15, 20, 25, 30, 35));

        data.put("datasets", datasets);

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);

        return ChartData.builder()
                .chartType("bar")
                .title("Ежемесячный отчет")
                .xAxisLabel("Месяц")
                .yAxisLabel("Значения")
                .data(data)
                .options(options)
                .build();
    }
}
package com.example.blps.service.notification;

import com.example.blps.dto.notification.ChartData;
import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.Dashboard;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.repository.data.MetricRepository;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.repository.notification.DashboardRepository;
import com.example.blps.service.data.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardGeneratorService {

    private final DashboardRepository dashboardRepository;
    private final OurCampaignRepository campaignRepository;
    private final DashboardSaveService dashboardSaveService;
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
            dashboard.setChartData(objectMapper.writeValueAsString(chartData.getData()));
            dashboard.setChartConfig(objectMapper.writeValueAsString(chartData.getOptions()));
            dashboard.setCreatedAt(LocalDateTime.now());
            dashboard.setIsPublished(request.getAutoPublish() != null && request.getAutoPublish());

            // Используем DashboardSaveService для сохранения
            Dashboard savedDashboard = dashboardSaveService.saveDashboardEntity(dashboard);
            log.info("Dashboard saved with ID: {}", savedDashboard.getId());

            // Отправляем уведомление о создании дашборда
            if (request.getRecipientsGroup() != null && !request.getRecipientsGroup().isEmpty()) {
                sendDashboardNotification(savedDashboard, request.getRecipientsGroup());
            }
        } catch (Exception e) {
            log.error("Error generating dashboard", e);
        }
    }

    /**
     * Генерирует данные для дашборда в зависимости от его типа
     */
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

    // Методы для генерации различных типов дашбордов

    private ChartData generateCampaignPerformanceChart() {
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

    private ChartData generateBudgetAllocationChart() {
        var campaigns = campaignRepository.findAll();

        Map<String, Object> data = new HashMap<>();
        data.put("labels", campaigns.stream().map(c -> c.getCampaignName()).collect(Collectors.toList()));
        data.put("values", campaigns.stream().map(c -> c.getBudget()).collect(Collectors.toList()));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("legend", Map.of("position", "right"));

        return ChartData.builder()
                .chartType("pie")
                .title("Распределение бюджета")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateRoiAnalysisChart() {
        var campaignReports = reportService.getCampaignsReportData();

        Map<String, Object> data = new HashMap<>();
        data.put("labels", campaignReports.stream().map(r -> r.getCampaignName()).collect(Collectors.toList()));
        data.put("values", campaignReports.stream().map(r -> r.getRoi()).collect(Collectors.toList()));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);
        options.put("scales", Map.of(
                "y", Map.of("beginAtZero", false)
        ));

        return ChartData.builder()
                .chartType("bar")
                .title("Анализ ROI")
                .xAxisLabel("Кампании")
                .yAxisLabel("ROI (%)")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateClickRatesChart() {
        var metrics = metricRepository.findByClickCountGreaterThan(0);

        Map<String, Object> data = new HashMap<>();
        data.put("labels", metrics.stream()
                .map(m -> m.getCampaign() != null ? m.getCampaign().getCampaignName() : "Unknown")
                .collect(Collectors.toList()));
        data.put("values", metrics.stream().map(m -> m.getClickCount()).collect(Collectors.toList()));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);

        return ChartData.builder()
                .chartType("line")
                .title("Количество кликов")
                .xAxisLabel("Кампании")
                .yAxisLabel("Клики")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateConversionRatesChart() {
        var campaignReports = reportService.getCampaignsReportData();

        Map<String, Object> data = new HashMap<>();
        data.put("labels", campaignReports.stream().map(r -> r.getCampaignName()).collect(Collectors.toList()));
        data.put("values", campaignReports.stream().map(r -> r.getConversionRate()).collect(Collectors.toList()));

        Map<String, Object> options = new HashMap<>();
        options.put("responsive", true);

        return ChartData.builder()
                .chartType("line")
                .title("Коэффициенты конверсии")
                .xAxisLabel("Кампании")
                .yAxisLabel("Конверсия (%)")
                .data(data)
                .options(options)
                .build();
    }

    private ChartData generateWeeklySummaryChart() {
        // Здесь можно добавить логику для генерации еженедельного отчета
        // Для примера просто создадим фиктивные данные

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
        // Здесь можно добавить логику для генерации ежемесячного отчета
        // Для примера просто создадим фиктивные данные

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
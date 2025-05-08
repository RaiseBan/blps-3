// src/main/java/com/example/blps/service/notification/SimplifiedDashboardService.java
package com.example.blps.service.notification;

import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.service.data.ReportService;
import com.example.blps.service.integration.Bitrix24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimplifiedDashboardService {

    private final ReportService reportService;
    private final Bitrix24Service bitrix24Service;
    private final MessageSenderService messageSenderService;
    private final ChartGeneratorService chartGeneratorService;

    public void processDashboardRequest(DashboardGenerationRequest request) {
        log.info("=== SimplifiedDashboardService processing request: {} ===", request);

        try {
            // 1. Получаем данные для графиков
            List<CampaignReportDTO> reports = reportService.getCampaignsReportData();

            if (reports.isEmpty()) {
                log.warn("No campaign data available for report generation");
                sendErrorNotification(request, new Exception("No campaign data available"));
                return;
            }

            // 2. Генерируем отчет с графиками
            String chartBase64 = generateChart(request.getType(), reports);

            // 3. Создаем текстовый отчет
            String reportText = generateTextReport(request.getType(), reports);

            // 4. Отправляем в Bitrix24
            sendToBitrix24(request, chartBase64, reportText);

            // 5. Отправляем уведомление об успешной обработке
            sendSuccessNotification(request);

            log.info("Dashboard generation completed successfully");
        } catch (Exception e) {
            log.error("Error generating dashboard", e);
            sendErrorNotification(request, e);
        }
    }

    private String generateChart(com.example.blps.model.notification.DashboardType type,
                                 List<CampaignReportDTO> reports) throws IOException {
        switch (type) {
            case ANALYTICS_REPORT:
                return chartGeneratorService.generateAnalyticsReport(reports);
            case FINANCIAL_REPORT:
                return chartGeneratorService.generateFinancialReport(reports);
            default:
                throw new IllegalArgumentException("Unsupported dashboard type: " + type);
        }
    }

    private String generateTextReport(com.example.blps.model.notification.DashboardType type,
                                      List<CampaignReportDTO> reports) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        report.append("Сгенерировано: ").append(LocalDateTime.now().format(formatter)).append("\n\n");

        if (type == com.example.blps.model.notification.DashboardType.ANALYTICS_REPORT) {
            report.append("=== АНАЛИТИЧЕСКИЙ ОТЧЕТ ===\n\n");

            int totalClicks = reports.stream().mapToInt(r -> r.getClickCount()).sum();
            double avgCtr = reports.stream().mapToDouble(r -> r.getCtr().doubleValue()).average().orElse(0.0);
            double avgConversion = reports.stream().mapToDouble(r -> r.getConversionRate().doubleValue()).average().orElse(0.0);

            report.append("Общая статистика:\n");
            report.append("- Всего кликов: ").append(totalClicks).append("\n");
            report.append("- Средний CTR: ").append(String.format("%.2f", avgCtr)).append("%\n");
            report.append("- Средняя конверсия: ").append(String.format("%.2f", avgConversion)).append("%\n\n");

            report.append("Детализация по кампаниям:\n");
            for (CampaignReportDTO dto : reports) {
                report.append("\n").append(dto.getCampaignName()).append(":\n");
                report.append("  - Клики: ").append(dto.getClickCount()).append("\n");
                report.append("  - CTR: ").append(String.format("%.2f", dto.getCtr())).append("%\n");
                report.append("  - Конверсия: ").append(String.format("%.2f", dto.getConversionRate())).append("%\n");
            }
        } else {
            report.append("=== ФИНАНСОВЫЙ ОТЧЕТ ===\n\n");

            double totalBudget = reports.stream().mapToDouble(r -> r.getBudget().doubleValue()).sum();
            double avgRoi = reports.stream().mapToDouble(r -> r.getRoi().doubleValue()).average().orElse(0.0);

            report.append("Общая статистика:\n");
            report.append("- Общий бюджет: ").append(String.format("%.2f", totalBudget)).append(" руб.\n");
            report.append("- Средний ROI: ").append(String.format("%.2f", avgRoi)).append("%\n\n");

            report.append("Детализация по кампаниям:\n");
            for (CampaignReportDTO dto : reports) {
                report.append("\n").append(dto.getCampaignName()).append(":\n");
                report.append("  - Бюджет: ").append(String.format("%.2f", dto.getBudget())).append(" руб.\n");
                report.append("  - ROI: ").append(String.format("%.2f", dto.getRoi())).append("%\n");
            }
        }

        return report.toString();
    }

    private void sendToBitrix24(DashboardGenerationRequest request, String chartBase64, String reportText) {
        try {
            String title = request.getTitle();

            // Полное описание для задачи
            StringBuilder fullDescription = new StringBuilder();
            fullDescription.append(reportText);
            fullDescription.append("\n\n");
            fullDescription.append("Тип отчета: ").append(request.getType()).append("\n");
            fullDescription.append("Период: ").append(request.getStartDate())
                    .append(" - ").append(request.getEndDate()).append("\n");

            // Отправляем задачу с изображением в Bitrix24
            String taskId = bitrix24Service.createTaskWithImage(
                    title,
                    fullDescription.toString(),
                    "1",
                    chartBase64,
                    request.getType() + "_report.png"
            );

            log.info("Report sent to Bitrix24 as task with ID: {}", taskId);
        } catch (Exception e) {
            log.error("Error sending report to Bitrix24", e);
            throw new RuntimeException("Failed to send report to Bitrix24", e);
        }
    }

    private void sendSuccessNotification(DashboardGenerationRequest request) {
        NotificationMessage notification = NotificationMessage.builder()
                .title("Отчет создан: " + request.getTitle())
                .message("Отчет успешно создан и отправлен в Bitrix24")
                .type(NotificationType.REPORT_GENERATED)
                .recipient(request.getRecipientsGroup())
                .build();

        messageSenderService.sendNotification(notification);
    }

    private void sendErrorNotification(DashboardGenerationRequest request, Exception e) {
        NotificationMessage notification = NotificationMessage.builder()
                .title("Ошибка создания отчета: " + request.getTitle())
                .message("Произошла ошибка: " + e.getMessage())
                .type(NotificationType.SYSTEM_ALERT)
                .recipient("ROLE_ADMIN")
                .build();

        messageSenderService.sendNotification(notification);
    }
}
// Обновленная версия SimplifiedDashboardService
package com.example.blps.service.notification;

import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.dto.notification.ChartData;
import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.service.data.ReportService;
import com.example.blps.service.integration.Bitrix24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimplifiedDashboardService {

    private final ReportService reportService;
    private final Bitrix24Service bitrix24Service;
    private final MessageSenderService messageSenderService;
    private final ChartGeneratorService chartGeneratorService;

    @JmsListener(destination = MessageSenderService.DASHBOARD_GENERATION_QUEUE)
    public void processDashboardRequest(DashboardGenerationRequest request) {
        log.info("Processing dashboard generation request: {}", request);

        try {
            // 1. Получаем данные для графиков
            List<CampaignReportDTO> reports = reportService.getCampaignsReportData();

            // 2. Генерируем графики
            byte[] chartImage = generateChart(request.getType(), reports);

            // 3. Отправляем в Bitrix24
            sendToBitrix24(request, chartImage);

            // 4. Отправляем уведомление об успешной обработке
            sendSuccessNotification(request);

            log.info("Dashboard generation completed successfully");
        } catch (Exception e) {
            log.error("Error generating dashboard", e);
            sendErrorNotification(request, e);
        }
    }

    private byte[] generateChart(com.example.blps.model.notification.DashboardType type,
                                 List<CampaignReportDTO> reports) throws IOException {
        switch (type) {
            case CAMPAIGN_PERFORMANCE:
                return chartGeneratorService.generateCampaignPerformanceChart(reports);
            case BUDGET_ALLOCATION:
                return chartGeneratorService.generateBudgetDistributionChart(reports);
            case ROI_ANALYSIS:
                return chartGeneratorService.generateRoiChart(reports);
            case WEEKLY_SUMMARY:
            case MONTHLY_REPORT:
                return chartGeneratorService.generateCampaignPerformanceChart(reports);
            default:
                throw new IllegalArgumentException("Unsupported dashboard type: " + type);
        }
    }

    private void sendToBitrix24(DashboardGenerationRequest request, byte[] chartImage) {
        try {
            String title = "Дашборд: " + request.getTitle();

            // Формируем описание для задачи
            StringBuilder description = new StringBuilder();
            description.append("Тип: ").append(request.getType()).append("\n");
            description.append("Описание: ").append(request.getDescription()).append("\n");
            description.append("Период: ").append(request.getStartDate())
                    .append(" - ").append(request.getEndDate()).append("\n");
            description.append("Создан: ").append(LocalDateTime.now()).append("\n\n");
            description.append("График прикреплен к задаче как файл.");

            // Отправляем задачу с изображением в Bitrix24
            String taskId = bitrix24Service.createTaskWithImage(
                    title,
                    description.toString(),
                    "1",
                    chartImage,
                    "dashboard_" + request.getType() + ".png"
            );

            log.info("Dashboard sent to Bitrix24 as task with ID: {}", taskId);
        } catch (Exception e) {
            log.error("Error sending dashboard to Bitrix24", e);
            throw new RuntimeException("Failed to send dashboard to Bitrix24", e);
        }
    }

    private void sendSuccessNotification(DashboardGenerationRequest request) {
        NotificationMessage notification = NotificationMessage.builder()
                .title("Дашборд создан: " + request.getTitle())
                .message("Дашборд успешно создан и отправлен в Bitrix24 с графиком")
                .type(NotificationType.DASHBOARD_CREATED)
                .recipient(request.getRecipientsGroup())
                .build();

        messageSenderService.sendNotification(notification);
    }

    private void sendErrorNotification(DashboardGenerationRequest request, Exception e) {
        NotificationMessage notification = NotificationMessage.builder()
                .title("Ошибка создания дашборда: " + request.getTitle())
                .message("Произошла ошибка при создании дашборда: " + e.getMessage())
                .type(NotificationType.SYSTEM_ALERT)
                .recipient("ROLE_ADMIN")
                .build();

        messageSenderService.sendNotification(notification);
    }
}
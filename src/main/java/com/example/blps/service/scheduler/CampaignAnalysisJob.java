package com.example.blps.service.scheduler;

import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.service.notification.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Задание для анализа эффективности кампаний и отправки уведомлений
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignAnalysisJob implements Job {
    
    private final OurCampaignRepository campaignRepository;
    private final MessageSenderService messageSenderService;
    
    // Пороговые значения для метрик
    private static final BigDecimal LOW_ROI_THRESHOLD = new BigDecimal("-5.0");
    private static final BigDecimal HIGH_ROI_THRESHOLD = new BigDecimal("20.0");
    private static final BigDecimal LOW_CTR_THRESHOLD = new BigDecimal("1.0");
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing campaign analysis job");
        
        try {
            List<OurCampaign> campaigns = campaignRepository.findAll();
            
            for (OurCampaign campaign : campaigns) {
                // Проверяем метрики только если они существуют
                if (campaign.getMetric() != null) {
                    analyzeCampaignMetrics(campaign);
                }
            }
            
            log.info("Campaign analysis job completed successfully");
            
        } catch (Exception e) {
            log.error("Error executing campaign analysis job", e);
            throw new JobExecutionException(e);
        }
    }
    
    /**
     * Анализирует метрики кампании и отправляет уведомления при необходимости
     */
    private void analyzeCampaignMetrics(OurCampaign campaign) {
        // Анализ ROI
        if (campaign.getMetric().getRoi() != null) {
            BigDecimal roi = campaign.getMetric().getRoi();
            
            if (roi.compareTo(LOW_ROI_THRESHOLD) < 0) {
                // Отправляем предупреждение о низком ROI
                sendLowRoiAlert(campaign);
            } else if (roi.compareTo(HIGH_ROI_THRESHOLD) > 0) {
                // Отправляем уведомление о высоком ROI (возможность увеличить бюджет)
                sendHighRoiAlert(campaign);
            }
        }
        
        // Анализ CTR
        if (campaign.getMetric().getCtr() != null) {
            BigDecimal ctr = campaign.getMetric().getCtr();
            
            if (ctr.compareTo(LOW_CTR_THRESHOLD) < 0) {
                // Отправляем предупреждение о низком CTR
                sendLowCtrAlert(campaign);
            }
        }
    }
    
    /**
     * Отправляет уведомление о низком ROI
     */
    private void sendLowRoiAlert(OurCampaign campaign) {
        String title = "Низкий ROI для кампании " + campaign.getCampaignName();
        String message = String.format(
                "Кампания '%s' имеет низкий показатель ROI: %s%%. Рекомендуется пересмотреть стратегию или уменьшить бюджет.",
                campaign.getCampaignName(),
                campaign.getMetric().getRoi().toString()
        );
        
        NotificationMessage notification = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SYSTEM_ALERT)
                .recipient("ROLE_CAMPAIGN_MANAGER,ROLE_ADMIN")
                .relatedEntityId(campaign.getId())
                .build();
        
        messageSenderService.sendNotification(notification);
    }
    
    /**
     * Отправляет уведомление о высоком ROI
     */
    private void sendHighRoiAlert(OurCampaign campaign) {
        String title = "Высокий ROI для кампании " + campaign.getCampaignName();
        String message = String.format(
                "Кампания '%s' демонстрирует высокий ROI: %s%%. Рекомендуется рассмотреть возможность увеличения бюджета.",
                campaign.getCampaignName(),
                campaign.getMetric().getRoi().toString()
        );
        
        NotificationMessage notification = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SYSTEM_ALERT)
                .recipient("ROLE_CAMPAIGN_MANAGER,ROLE_ADMIN")
                .relatedEntityId(campaign.getId())
                .build();
        
        messageSenderService.sendNotification(notification);
    }
    
    /**
     * Отправляет уведомление о низком CTR
     */
    private void sendLowCtrAlert(OurCampaign campaign) {
        String title = "Низкий CTR для кампании " + campaign.getCampaignName();
        String message = String.format(
                "Кампания '%s' имеет низкий показатель CTR: %s%%. Рекомендуется улучшить креативы или пересмотреть таргетинг.",
                campaign.getCampaignName(),
                campaign.getMetric().getCtr().toString()
        );
        
        NotificationMessage notification = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SYSTEM_ALERT)
                .recipient("ROLE_CAMPAIGN_MANAGER,ROLE_ADMIN")
                .relatedEntityId(campaign.getId())
                .build();
        
        messageSenderService.sendNotification(notification);
    }
}
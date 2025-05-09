package com.example.blps.service.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.controllers.utils.CampaignMapper;
import com.example.blps.service.integration.Bitrix24Service;
import com.example.blps.service.notification.MessageSenderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetOptimizationService {

    private final OurCampaignRepository campaignRepository;
    private final PlatformTransactionManager transactionManager;
    private final CampaignMapper campaignMapper;
    private final MessageSenderService messageSenderService;
    private final Bitrix24Service bitrix24Service;

    private static final BigDecimal HIGH_ROI_THRESHOLD = new BigDecimal("25.0");
    private static final BigDecimal LOW_ROI_THRESHOLD = new BigDecimal("0.0");
    private static final BigDecimal HIGH_ROI_INCREASE_FACTOR = new BigDecimal("1.20");  
    private static final BigDecimal LOW_ROI_DECREASE_FACTOR = new BigDecimal("0.85");   
    private static final BigDecimal NEUTRAL_ROI_FACTOR = new BigDecimal("1.05");        

    public OurCampaignDTO optimizeCampaignBudget(Long campaignId) {
        
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("optimizeCampaignBudgetTransaction");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = transactionManager.getTransaction(definition);

        try {
            
            OurCampaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new NotFoundException("Кампания не найдена"));

            BigDecimal oldBudget = campaign.getBudget();
            BigDecimal optimizedBudget = calculateOptimizedBudget(campaign);

            log.info("Оптимизация бюджета кампании {} (ID: {}): {} -> {}",
                    campaign.getCampaignName(), campaign.getId(), oldBudget, optimizedBudget);

            campaign.setBudget(optimizedBudget);
            OurCampaign savedCampaign = campaignRepository.save(campaign);

            transactionManager.commit(status);

            sendBudgetOptimizationNotification(campaign, oldBudget, optimizedBudget);

            try {
                bitrix24Service.syncBudgetOptimization(
                        campaignId,
                        campaign.getCampaignName(),
                        oldBudget,
                        optimizedBudget
                );
            } catch (Exception e) {
                log.error("Ошибка при отправке данных об оптимизации бюджета в Bitrix24", e);
                
            }

            return campaignMapper.toDTO(savedCampaign);

        } catch (Exception e) {
            
            transactionManager.rollback(status);
            log.error("Ошибка при оптимизации бюджета кампании: {}", e.getMessage());
            throw e;
        }
    }

    private void sendBudgetOptimizationNotification(OurCampaign campaign, BigDecimal oldBudget, BigDecimal newBudget) {
        
        BigDecimal percentChange = calculatePercentChange(oldBudget, newBudget);

        String title = "Оптимизирован бюджет кампании: " + campaign.getCampaignName();
        String message = String.format(
                "Произведена автоматическая оптимизация бюджета для кампании '%s'.\n" +
                        "Старый бюджет: %s\n" +
                        "Новый бюджет: %s\n" +
                        "Изменение: %s%%",
                campaign.getCampaignName(),
                oldBudget,
                newBudget,
                percentChange
        );

        NotificationMessage notification = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(NotificationType.BUDGET_OPTIMIZED)
                .recipient("ROLE_ADMIN,ROLE_CAMPAIGN_MANAGER,ROLE_ANALYST")
                .relatedEntityId(campaign.getId())
                .build();

        messageSenderService.sendNotification(notification);
    }

    private BigDecimal calculatePercentChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100"); 
        }

        return newValue.subtract(oldValue)
                .multiply(new BigDecimal("100"))
                .divide(oldValue, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateOptimizedBudget(OurCampaign campaign) {
        Metric metric = campaign.getMetric();
        BigDecimal currentBudget = campaign.getBudget();

        if (metric == null || currentBudget == null || currentBudget.compareTo(BigDecimal.ZERO) == 0) {
            return currentBudget != null ? currentBudget : BigDecimal.ZERO;
        }

        BigDecimal roi = metric.getRoi();
        BigDecimal ctr = metric.getCtr();
        BigDecimal conversionRate = metric.getConversionRate();

        if (roi == null) {
            BigDecimal estimatedFactor = estimateFactorFromOtherMetrics(ctr, conversionRate);
            return currentBudget.multiply(estimatedFactor)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal budgetFactor;

        if (roi.compareTo(HIGH_ROI_THRESHOLD) > 0) {
            
            budgetFactor = HIGH_ROI_INCREASE_FACTOR;
        } else if (roi.compareTo(LOW_ROI_THRESHOLD) < 0) {
            
            budgetFactor = LOW_ROI_DECREASE_FACTOR;
        } else {
            
            budgetFactor = NEUTRAL_ROI_FACTOR;
        }

        budgetFactor = adjustFactorBasedOnMetrics(budgetFactor, ctr, conversionRate);

        return currentBudget.multiply(budgetFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal adjustFactorBasedOnMetrics(BigDecimal factor, BigDecimal ctr, BigDecimal conversionRate) {
        
        if (ctr == null || conversionRate == null) {
            return factor;
        }

        BigDecimal adjustment = BigDecimal.ONE;

        if (ctr.compareTo(new BigDecimal("5.0")) > 0) {
            adjustment = adjustment.add(new BigDecimal("0.03"));
        }

        if (conversionRate.compareTo(new BigDecimal("2.0")) > 0) {
            adjustment = adjustment.add(new BigDecimal("0.02"));
        }

        return factor.multiply(adjustment)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal estimateFactorFromOtherMetrics(BigDecimal ctr, BigDecimal conversionRate) {
        
        if (ctr == null || conversionRate == null) {
            return BigDecimal.ONE;
        }

        BigDecimal estimatedFactor = BigDecimal.ONE;

        if (ctr.compareTo(new BigDecimal("5.0")) > 0) {
            estimatedFactor = estimatedFactor.add(new BigDecimal("0.1"));
        } else if (ctr.compareTo(new BigDecimal("1.0")) < 0) {
            
            estimatedFactor = estimatedFactor.subtract(new BigDecimal("0.05"));
        }

        if (conversionRate.compareTo(new BigDecimal("2.0")) > 0) {
            estimatedFactor = estimatedFactor.add(new BigDecimal("0.1"));
        } else if (conversionRate.compareTo(new BigDecimal("0.5")) < 0) {
            
            estimatedFactor = estimatedFactor.subtract(new BigDecimal("0.05"));
        }

        return estimatedFactor;
    }
}
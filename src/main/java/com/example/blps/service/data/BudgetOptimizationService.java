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
    private static final BigDecimal HIGH_ROI_INCREASE_FACTOR = new BigDecimal("1.20");  // +20%
    private static final BigDecimal LOW_ROI_DECREASE_FACTOR = new BigDecimal("0.85");   // -15%
    private static final BigDecimal NEUTRAL_ROI_FACTOR = new BigDecimal("1.05");        // +5%

    /**
     * Оптимизирует бюджет кампании на основе аналитических данных
     *
     * @param campaignId ID кампании для оптимизации
     * @return Обновленную информацию о кампании с оптимизированным бюджетом
     */
    public OurCampaignDTO optimizeCampaignBudget(Long campaignId) {
        // Определение транзакции с использованием JTA
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("optimizeCampaignBudgetTransaction");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // Начало транзакции через Atomikos JTA менеджер
        TransactionStatus status = transactionManager.getTransaction(definition);

        try {
            // Получение данных кампании внутри транзакции
            OurCampaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new NotFoundException("Кампания не найдена"));

            // Анализ метрик и расчет нового бюджета
            BigDecimal oldBudget = campaign.getBudget();
            BigDecimal optimizedBudget = calculateOptimizedBudget(campaign);

            // Логирование изменений для аудита
            log.info("Оптимизация бюджета кампании {} (ID: {}): {} -> {}",
                    campaign.getCampaignName(), campaign.getId(), oldBudget, optimizedBudget);

            // Обновление бюджета кампании
            campaign.setBudget(optimizedBudget);
            OurCampaign savedCampaign = campaignRepository.save(campaign);

            // Коммит транзакции
            transactionManager.commit(status);

            // Отправка уведомления
            sendBudgetOptimizationNotification(campaign, oldBudget, optimizedBudget);

            // Отправка информации в Bitrix24
            try {
                bitrix24Service.syncBudgetOptimization(
                        campaignId,
                        campaign.getCampaignName(),
                        oldBudget,
                        optimizedBudget
                );
            } catch (Exception e) {
                log.error("Ошибка при отправке данных об оптимизации бюджета в Bitrix24", e);
                // Не прерываем основной процесс из-за ошибки интеграции
            }

            return campaignMapper.toDTO(savedCampaign);

        } catch (Exception e) {
            // Откат транзакции в случае ошибки
            transactionManager.rollback(status);
            log.error("Ошибка при оптимизации бюджета кампании: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Отправляет уведомление об оптимизации бюджета
     */
    private void sendBudgetOptimizationNotification(OurCampaign campaign, BigDecimal oldBudget, BigDecimal newBudget) {
        // Рассчитываем процент изменения
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

    /**
     * Рассчитывает процентное изменение
     */
    private BigDecimal calculatePercentChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100"); // Если старое значение было 0
        }

        return newValue.subtract(oldValue)
                .multiply(new BigDecimal("100"))
                .divide(oldValue, 2, RoundingMode.HALF_UP);
    }

    /**
     * Рассчитывает оптимальный бюджет на основе метрик кампании
     */
    private BigDecimal calculateOptimizedBudget(OurCampaign campaign) {
        Metric metric = campaign.getMetric();
        BigDecimal currentBudget = campaign.getBudget();

        // Если метрики отсутствуют или бюджет равен нулю, возвращаем текущий бюджет
        if (metric == null || currentBudget == null || currentBudget.compareTo(BigDecimal.ZERO) == 0) {
            return currentBudget != null ? currentBudget : BigDecimal.ZERO;
        }

        BigDecimal roi = metric.getRoi();
        BigDecimal ctr = metric.getCtr();
        BigDecimal conversionRate = metric.getConversionRate();

        // Если ROI отсутствует, используем другие метрики для приблизительной оценки
        if (roi == null) {
            BigDecimal estimatedFactor = estimateFactorFromOtherMetrics(ctr, conversionRate);
            return currentBudget.multiply(estimatedFactor)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Определение коэффициента изменения бюджета на основе ROI
        BigDecimal budgetFactor;

        if (roi.compareTo(HIGH_ROI_THRESHOLD) > 0) {
            // Высокий ROI - увеличиваем бюджет
            budgetFactor = HIGH_ROI_INCREASE_FACTOR;
        } else if (roi.compareTo(LOW_ROI_THRESHOLD) < 0) {
            // Отрицательный ROI - снижаем бюджет
            budgetFactor = LOW_ROI_DECREASE_FACTOR;
        } else {
            // Нейтральный ROI - небольшое увеличение
            budgetFactor = NEUTRAL_ROI_FACTOR;
        }

        // Корректировка фактора на основе CTR и коэффициента конверсии
        budgetFactor = adjustFactorBasedOnMetrics(budgetFactor, ctr, conversionRate);

        // Расчет нового бюджета с округлением до 2 знаков после запятой
        return currentBudget.multiply(budgetFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Корректирует коэффициент изменения бюджета с учетом CTR и конверсии
     */
    private BigDecimal adjustFactorBasedOnMetrics(BigDecimal factor, BigDecimal ctr, BigDecimal conversionRate) {
        // Если метрики отсутствуют, возвращаем исходный фактор
        if (ctr == null || conversionRate == null) {
            return factor;
        }

        // Используем CTR и конверсию для небольшой корректировки (±5%)
        BigDecimal adjustment = BigDecimal.ONE;

        // Хороший CTR (>5%) - дополнительное увеличение
        if (ctr.compareTo(new BigDecimal("5.0")) > 0) {
            adjustment = adjustment.add(new BigDecimal("0.03"));
        }

        // Хорошая конверсия (>2%) - дополнительное увеличение
        if (conversionRate.compareTo(new BigDecimal("2.0")) > 0) {
            adjustment = adjustment.add(new BigDecimal("0.02"));
        }

        // Корректировка фактора с учетом дополнительных метрик
        return factor.multiply(adjustment)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Оценивает фактор изменения бюджета на основе CTR и конверсии, если ROI
     * отсутствует
     */
    private BigDecimal estimateFactorFromOtherMetrics(BigDecimal ctr, BigDecimal conversionRate) {
        // Если метрики отсутствуют, возвращаем нейтральный фактор
        if (ctr == null || conversionRate == null) {
            return BigDecimal.ONE;
        }

        // Оценка на основе CTR и конверсии
        BigDecimal estimatedFactor = BigDecimal.ONE;

        // Хороший CTR (>5%) - увеличение
        if (ctr.compareTo(new BigDecimal("5.0")) > 0) {
            estimatedFactor = estimatedFactor.add(new BigDecimal("0.1"));
        } else if (ctr.compareTo(new BigDecimal("1.0")) < 0) {
            // Плохой CTR (<1%) - уменьшение
            estimatedFactor = estimatedFactor.subtract(new BigDecimal("0.05"));
        }

        // Хорошая конверсия (>2%) - увеличение
        if (conversionRate.compareTo(new BigDecimal("2.0")) > 0) {
            estimatedFactor = estimatedFactor.add(new BigDecimal("0.1"));
        } else if (conversionRate.compareTo(new BigDecimal("0.5")) < 0) {
            // Плохая конверсия (<0.5%) - уменьшение
            estimatedFactor = estimatedFactor.subtract(new BigDecimal("0.05"));
        }

        return estimatedFactor;
    }
}
package com.example.blps.service.scheduler;

import com.example.blps.dto.billing.BillingRequest;
import com.example.blps.dto.billing.BillingType;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.service.billing.BillingSenderService;
import com.example.blps.service.integration.Bitrix24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "financial.node.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final OurCampaignRepository campaignRepository;
    private final BillingSenderService billingSenderService;

    @Scheduled(fixedDelay = 120000, initialDelay = 10000)
    public void generateBills() {
        log.info("=== ЗАПУСК ГЕНЕРАЦИИ СЧЕТОВ ===");
        log.info("Время: {}", LocalDateTime.now());

        try {
            List<OurCampaign> campaigns = campaignRepository.findAll();
            log.info("Найдено кампаний: {}", campaigns.size());

            for (OurCampaign campaign : campaigns) {
                if (shouldGenerateBill(campaign)) {
                    BillingRequest request = createBillingRequest(campaign);
                    billingSenderService.sendBillingRequest(request);
                    log.info("Отправлен запрос на генерацию счета для кампании: {}",
                            campaign.getCampaignName());
                }
            }

            log.info("=== ОТПРАВКА ЗАПРОСОВ НА ГЕНЕРАЦИЮ СЧЕТОВ ЗАВЕРШЕНА ===");
        } catch (Exception e) {
            log.error("Ошибка при отправке запросов на генерацию счетов", e);
        }
    }

    private boolean shouldGenerateBill(OurCampaign campaign) {
        return campaign.getMetric() != null && campaign.getMetric().getClickCount() > 0;
    }

    private BillingRequest createBillingRequest(OurCampaign campaign) {
        return BillingRequest.builder()
                .campaignId(campaign.getId())
                .periodStart(LocalDate.now().minusMonths(1))
                .periodEnd(LocalDate.now())
                .requestedBy("system-scheduler")
                .billingType(BillingType.MONTHLY)
                .build();
    }
}
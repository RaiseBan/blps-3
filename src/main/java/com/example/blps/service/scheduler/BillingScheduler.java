package com.example.blps.service.scheduler;

import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.service.integration.Bitrix24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {
    
    private final OurCampaignRepository campaignRepository;
    private final Bitrix24Service bitrix24Service;
    
    // Каждые 5 минут
    @Scheduled(fixedDelay = 300000, initialDelay = 10000)
    public void generateBills() {
        log.info("=== ЗАПУСК ГЕНЕРАЦИИ СЧЕТОВ ===");
        log.info("Время: {}", LocalDateTime.now());
        
        try {
            List<OurCampaign> campaigns = campaignRepository.findAll();
            log.info("Найдено кампаний: {}", campaigns.size());
            
            for (OurCampaign campaign : campaigns) {
                if (campaign.getMetric() != null && campaign.getMetric().getClickCount() > 0) {
                    generateBillForCampaign(campaign);
                }
            }
            
            log.info("=== ГЕНЕРАЦИЯ СЧЕТОВ ЗАВЕРШЕНА ===");
        } catch (Exception e) {
            log.error("Ошибка генерации счетов", e);
        }
    }
    
    private void generateBillForCampaign(OurCampaign campaign) {
        try {
            log.info("Генерация счета для кампании: {}", campaign.getCampaignName());
            
            // Простой расчет: количество кликов * 0.5 рубля
            BigDecimal amount = new BigDecimal(campaign.getMetric().getClickCount())
                    .multiply(new BigDecimal("0.5"));
            
            // Формируем текст счета
            String invoiceText = String.format(
                "СЧЕТ НА ОПЛАТУ\n" +
                "================\n" +
                "Кампания: %s\n" +
                "Кликов: %d\n" +
                "Стоимость за клик: 0.50 руб.\n" +
                "ИТОГО: %.2f руб.\n" +
                "Дата: %s",
                campaign.getCampaignName(),
                campaign.getMetric().getClickCount(),
                amount,
                LocalDateTime.now()
            );
            
            // Отправляем в Bitrix24
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("fields[TITLE]", "Счет: " + campaign.getCampaignName());
            params.put("fields[OPPORTUNITY]", amount.toString());
            params.put("fields[CURRENCY_ID]", "RUB");
            params.put("fields[STATUS_ID]", "NEW");
            params.put("fields[COMMENTS]", invoiceText);
            
            String result = bitrix24Service.connector.executeMethod("crm.invoice.add", params);
            log.info("Счет создан в Bitrix24: {}", result);
            
        } catch (Exception e) {
            log.error("Ошибка создания счета для кампании {}", campaign.getCampaignName(), e);
        }
    }
}
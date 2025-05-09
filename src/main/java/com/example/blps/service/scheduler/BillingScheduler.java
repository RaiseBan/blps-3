package com.example.blps.service.scheduler;

import com.example.blps.dto.billing.BillingRequest;
import com.example.blps.dto.billing.BillingType;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.service.billing.BillingSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "financial.node.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final Scheduler scheduler;

    @PostConstruct
    public void initializeJob() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(BillingJob.class)
                    .withIdentity("billingJob", "billingGroup")
                    .storeDurably()
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("billingTrigger", "billingGroup")
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(2) 
                            .repeatForever())
                    .startAt(DateBuilder.futureDate(10, DateBuilder.IntervalUnit.SECOND))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Billing job scheduled with 2 minutes interval");

        } catch (Exception e) {
            log.error("Error scheduling billing job", e);
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    public static class BillingJob extends BaseQuartzJob {

        @Override
        protected void executeInternal(JobExecutionContext context) {
            log.info("=== ЗАПУСК ГЕНЕРАЦИИ СЧЕТОВ ===");
            log.info("Время: {}", LocalDateTime.now());

            try {
                OurCampaignRepository campaignRepository = applicationContext.getBean(OurCampaignRepository.class);
                BillingSenderService billingSenderService = applicationContext.getBean(BillingSenderService.class);

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
}
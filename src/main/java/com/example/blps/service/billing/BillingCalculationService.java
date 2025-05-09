package com.example.blps.service.billing;

import com.example.blps.dto.billing.BillingRequest;
import com.example.blps.model.billing.BillingData;
import com.example.blps.model.billing.BillingItem;
import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.repository.data.OurCampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingCalculationService {

    private final OurCampaignRepository campaignRepository;

    private static final BigDecimal COST_PER_CLICK = new BigDecimal("0.50");
    private static final BigDecimal COST_PER_CONVERSION = new BigDecimal("5.00");
    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("10"); 

    public BillingData calculateBilling(BillingRequest request) {
        log.info("Calculating billing for campaign: {} for period {} - {}",
                request.getCampaignId(), request.getPeriodStart(), request.getPeriodEnd());

        OurCampaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        Metric metric = campaign.getMetric();
        if (metric == null) {
            return createEmptyBilling(campaign, request);
        }

        List<BillingItem> items = new ArrayList<>();
        BigDecimal totalSpent = BigDecimal.ZERO;

        if (metric.getClickCount() > 0) {
            BigDecimal clicksCost = COST_PER_CLICK.multiply(new BigDecimal(metric.getClickCount()));
            items.add(BillingItem.builder()
                    .description("Клики по рекламе")
                    .quantity(metric.getClickCount())
                    .unitPrice(COST_PER_CLICK)
                    .totalPrice(clicksCost)
                    .type("CLICK")
                    .build());
            totalSpent = totalSpent.add(clicksCost);
        }

        Integer conversions = calculateConversions(metric);
        if (conversions > 0) {
            BigDecimal conversionsCost = COST_PER_CONVERSION.multiply(new BigDecimal(conversions));
            items.add(BillingItem.builder()
                    .description("Конверсии")
                    .quantity(conversions)
                    .unitPrice(COST_PER_CONVERSION)
                    .totalPrice(conversionsCost)
                    .type("CONVERSION")
                    .build());
            totalSpent = totalSpent.add(conversionsCost);
        }

        BigDecimal platformFee = totalSpent.multiply(PLATFORM_FEE_PERCENTAGE)
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        items.add(BillingItem.builder()
                .description("Комиссия платформы")
                .quantity(1)
                .unitPrice(platformFee)
                .totalPrice(platformFee)
                .type("FEE")
                .build());

        totalSpent = totalSpent.add(platformFee);

        return BillingData.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getCampaignName())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .totalSpent(totalSpent)
                .clickCost(COST_PER_CLICK)
                .totalClicks(metric.getClickCount())
                .conversionCost(COST_PER_CONVERSION)
                .totalConversions(conversions)
                .status("PENDING")
                .generatedAt(LocalDateTime.now())
                .items(items)
                .build();
    }

    private Integer calculateConversions(Metric metric) {
        
        if (metric.getClickCount() == null || metric.getConversionRate() == null) {
            return 0;
        }

        BigDecimal conversions = new BigDecimal(metric.getClickCount())
                .multiply(metric.getConversionRate())
                .divide(new BigDecimal("100"), 0, BigDecimal.ROUND_HALF_UP);

        return conversions.intValue();
    }

    private BillingData createEmptyBilling(OurCampaign campaign, BillingRequest request) {
        return BillingData.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getCampaignName())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .totalSpent(BigDecimal.ZERO)
                .totalClicks(0)
                .totalConversions(0)
                .status("EMPTY")
                .generatedAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }
}
package com.example.blps.service.data;

import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReferralService {
    private final OurCampaignService campaignService;
    private final MetricService metricService;

    @Transactional
    public void processReferralClick(String referralHash) {
        OurCampaign campaign = campaignService.findByReferralHash(referralHash)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        Metric metric = getOrCreateMetric(campaign);
        updateMetrics(metric);
    }

    private Metric getOrCreateMetric(OurCampaign campaign) {
        return Optional.ofNullable(campaign.getMetric())
                .orElseGet(() -> createNewMetric(campaign));
    }

    private Metric createNewMetric(OurCampaign campaign) {
        Metric newMetric = new Metric();
        newMetric.setCampaign(campaign);
        campaign.setMetric(newMetric);
        return metricService.saveMetric(newMetric);
    }

    private void updateMetrics(Metric metric) {
        metric.setClickCount(metric.getClickCount() + 1);
        metric.setCtr(calculateCTR());
        metric.setConversionRate(calculateConversionRate());
        metric.setRoi(calculateROI());
    }

    // Эти методы можно вынести в отдельный Calculator сервис при необходимости
    private BigDecimal calculateCTR() {
        return BigDecimal.valueOf(Math.random() * 10);
    }

    private BigDecimal calculateConversionRate() {
        return BigDecimal.valueOf(Math.random() * 5);
    }

    private BigDecimal calculateROI() {
        return BigDecimal.valueOf(Math.random() * 100 - 50);
    }
}
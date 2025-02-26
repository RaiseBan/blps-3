package com.example.blps.controllers;

import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.service.data.MetricService;
import com.example.blps.service.data.OurCampaignService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/redirect")
public class RedirectController {

    private final OurCampaignService campaignService;
    private final MetricService metricService;

    public RedirectController(OurCampaignService campaignService, MetricService metricService) {
        this.campaignService = campaignService;
        this.metricService = metricService;
    }

    @GetMapping("/{referralHash}")
    public RedirectView handleRedirect(@PathVariable String referralHash) {
        OurCampaign campaign = campaignService.findByReferralHash(referralHash)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        Metric metric = Optional.ofNullable(campaign.getMetric())
                .orElseGet(() -> {
                    Metric newMetric = new Metric();
                    newMetric.setCampaign(campaign);
                    campaign.setMetric(newMetric);
                    return newMetric;
                });

        updateMetrics(metric);
        return new RedirectView("/");
    }

    @Transactional
    void updateMetrics(Metric metric) {
        metric.setClickCount(metric.getClickCount() + 1);
        metric.setCtr(BigDecimal.valueOf(Math.random() * 10));
        metric.setConversionRate(BigDecimal.valueOf(Math.random() * 5));
        metric.setRoi(BigDecimal.valueOf(Math.random() * 100 - 50));
        metricService.saveMetric(metric);
    }
}
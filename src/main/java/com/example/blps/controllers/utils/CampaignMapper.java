package com.example.blps.controllers.utils;

import com.example.blps.dto.data.MetricDTO;
import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.dto.data.OurCampaignRequest;
import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import org.springframework.stereotype.Component;

@Component
public class CampaignMapper {
    
    private static final String REDIRECT_BASE_URL = "http://localhost:8080/redirect/";

    public OurCampaignDTO toDTO(OurCampaign entity) {
        OurCampaignDTO dto = new OurCampaignDTO();
        dto.setId(entity.getId());
        dto.setCampaignName(entity.getCampaignName());
        dto.setReferralLink(REDIRECT_BASE_URL + entity.getReferralLink());
        dto.setBudget(entity.getBudget());
        dto.setPlacementUrl(entity.getPlacementUrl());
        dto.setMetric(toMetricDTO(entity.getMetric()));
        return dto;
    }
    private MetricDTO toMetricDTO(Metric metric) {
        if (metric == null) return null;

        MetricDTO dto = new MetricDTO();
        dto.setId(metric.getId());
        dto.setClickCount(metric.getClickCount());
        dto.setCtr(metric.getCtr());
        dto.setConversionRate(metric.getConversionRate());
        dto.setRoi(metric.getRoi());
        return dto;
    }

    public OurCampaign toEntity(OurCampaignRequest request) {
        OurCampaign entity = new OurCampaign();
        entity.setCampaignName(request.getCampaignName());
        entity.setBudget(request.getBudget());
        entity.setPlacementUrl(request.getPlacementUrl());
        return entity;
    }
}
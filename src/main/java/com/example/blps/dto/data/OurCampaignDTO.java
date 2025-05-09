package com.example.blps.dto.data;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OurCampaignDTO {
    private Long id;
    private String campaignName;
    private String referralLink;
    private BigDecimal budget;
    private String placementUrl;
    private MetricDTO metric; 
}
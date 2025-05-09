package com.example.blps.dto.data;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class CampaignReportDTO {
    private String campaignName;
    private BigDecimal budget;
    private Integer clickCount;
    private BigDecimal ctr;
    private BigDecimal conversionRate;
    private BigDecimal roi;
}
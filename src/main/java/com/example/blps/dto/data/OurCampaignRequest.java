package com.example.blps.dto.data;// OurCampaignRequest.java (для входящих данных)
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Getter
@Setter
public class OurCampaignRequest {
    @NotBlank
    private String campaignName;
    
    @PositiveOrZero
    private BigDecimal budget;
    
    @URL
    private String placementUrl;
}
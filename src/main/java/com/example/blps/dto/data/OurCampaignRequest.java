package com.example.blps.dto.data;// OurCampaignRequest.java (для входящих данных)
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OurCampaignRequest {
    @NotBlank
    private String campaignName;
    
    @PositiveOrZero
    private BigDecimal budget;


    @Pattern(
            regexp = "^(https?|ftp)://[A-Za-z0-9.-]+\\.[A-Za-z]{2,}.*$",
            message = "Invalid URL format"
    )
    private String placementUrl;
}
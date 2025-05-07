package com.example.blps.model.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long campaignId;
    private String campaignName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalSpent;
    private BigDecimal clickCost;
    private Integer totalClicks;
    private BigDecimal conversionCost;
    private Integer totalConversions;
    private String status;
    private LocalDateTime generatedAt;
    private List<BillingItem> items;
}
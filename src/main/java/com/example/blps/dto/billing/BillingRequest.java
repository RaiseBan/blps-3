package com.example.blps.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long campaignId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String requestedBy;
    private BillingType billingType;
}
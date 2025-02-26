package com.example.blps.dto.data;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

// TheirCampaignRequest.java
@Getter
@Setter
public class TheirCampaignRequest {
    @NotBlank
    private String partnerName;
    
    @URL
    private String imageUrl;
    
    @FutureOrPresent
    private LocalDate startDate;
    
    @Future
    private LocalDate endDate;
}
package com.example.blps.dto.data;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Getter
@Setter
public class TheirCampaignRequest {
    @NotBlank
    private String partnerName;

    @Pattern(
            regexp = "^(https?|ftp)://[A-Za-z0-9.-]+\\.[A-Za-z]{2,}.*$",
            message = "Invalid URL format"
    )
    private String imageUrl;
    
    @FutureOrPresent
    private LocalDate startDate;
    
    @Future
    private LocalDate endDate;
}
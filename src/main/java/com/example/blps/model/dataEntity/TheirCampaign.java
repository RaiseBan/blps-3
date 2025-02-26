package com.example.blps.model.dataEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TheirCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String partnerName;

    @URL
    private String imageUrl;

    @FutureOrPresent
    private LocalDate startDate;

    private LocalDate endDate;

    @AssertTrue
    private boolean isPeriodValid() {
        return endDate == null || !endDate.isBefore(startDate);
    }
}
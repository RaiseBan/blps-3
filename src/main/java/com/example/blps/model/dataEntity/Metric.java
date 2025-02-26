package com.example.blps.model.dataEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @PositiveOrZero
    private Integer clickCount = 0;

    @PositiveOrZero
    @Column(precision = 5, scale = 2)
    private BigDecimal ctr = BigDecimal.ZERO;

    @PositiveOrZero
    @Column(precision = 5, scale = 2)
    private BigDecimal conversionRate = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal roi = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "campaign_id")
    @JsonBackReference
    private OurCampaign campaign;
}
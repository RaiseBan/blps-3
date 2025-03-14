package com.example.blps.model.dataEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OurCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String campaignName;

    @Column(nullable = false, unique = true)
    private String referralLink;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(columnDefinition = "TEXT")
    private String placementUrl;

    @OneToOne(
            mappedBy = "campaign",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER // Для гарантированной загрузки
    )
    @JsonManagedReference
    private Metric metric;

    @PrePersist
    private void initializeMetric() {
        // Создаем метрику, если она еще не была создана
        if (this.metric == null) {
            this.metric = new Metric();
            this.metric.setCampaign(this);
        }
    }
}
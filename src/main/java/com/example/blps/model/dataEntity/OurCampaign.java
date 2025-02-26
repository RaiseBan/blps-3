package com.example.blps.model.dataEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.hash.Hashing;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    @URL
    private String placementUrl;

    @OneToOne(
            mappedBy = "campaign",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER // Для гарантированной загрузки
    )
    @JsonManagedReference
    private Metric metric;

    private void generateReferralLink() {
        try {
            String base = campaignName + Instant.now().toEpochMilli();
            String hash = Hashing.sha256()
                    .hashString(base, StandardCharsets.UTF_8)
                    .toString();
            // Сохраняем только хэш, без полного URL
            this.referralLink = hash.substring(0, 12);
        } catch (Exception e) {
            throw new RuntimeException("Error generating referral link", e);
        }
    }
    @PrePersist
    private void generateReferralLinkAndMetric() {
        generateReferralLink();

        if (this.metric == null) {
            this.metric = new Metric();
            this.metric.setCampaign(this);
        }
    }
}
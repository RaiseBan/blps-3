package com.example.blps.repository.data;

import com.example.blps.model.dataEntity.OurCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OurCampaignRepository extends JpaRepository<OurCampaign, Long> {
    boolean existsByCampaignName(String campaignName);
    boolean existsByReferralLink(String referralLink);
    Optional<OurCampaign> findByReferralLink(String referralLink);
}
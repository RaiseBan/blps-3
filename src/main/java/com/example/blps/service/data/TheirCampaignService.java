package com.example.blps.service.data;// TheirCampaignService.java
import com.example.blps.model.dataEntity.TheirCampaign;
import com.example.blps.repository.data.TheirCampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TheirCampaignService {

    private final TheirCampaignRepository theirCampaignRepository;

    public TheirCampaignService(TheirCampaignRepository theirCampaignRepository) {
        this.theirCampaignRepository = theirCampaignRepository;
    }

    public List<TheirCampaign> getAllCampaigns() {
        return theirCampaignRepository.findAll();
    }

    public Optional<TheirCampaign> getCampaignById(Long id) {
        return theirCampaignRepository.findById(id);
    }

    public TheirCampaign createCampaign(TheirCampaign campaign) {
        return theirCampaignRepository.save(campaign);
    }

    public Optional<TheirCampaign> updateCampaign(Long id, TheirCampaign campaignDetails) {
        return theirCampaignRepository.findById(id)
                .map(existing -> {
                    existing.setImageUrl(campaignDetails.getImageUrl());
                    existing.setStartDate(campaignDetails.getStartDate());
                    existing.setEndDate(campaignDetails.getEndDate());
                    return theirCampaignRepository.save(existing);
                });
    }

    public boolean deleteCampaign(Long id) {
        if (theirCampaignRepository.existsById(id)) {
            theirCampaignRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
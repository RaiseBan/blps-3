package com.example.blps.service.data;// TheirCampaignService.java
import com.example.blps.dto.data.TheirCampaignRequest;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.CampaignStatus;
import com.example.blps.model.dataEntity.TheirCampaign;
import com.example.blps.repository.data.TheirCampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    public TheirCampaign createCampaign(TheirCampaignRequest request) {
        TheirCampaign campaign = new TheirCampaign();
        campaign.setPartnerName(request.getPartnerName());
        campaign.setImageUrl(request.getImageUrl());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());

        // Автоматическое определение статуса
        LocalDate today = LocalDate.now();
        if (!today.isBefore(request.getStartDate()) && !today.isAfter(request.getEndDate())) {
            campaign.setStatus(CampaignStatus.ACTIVE);
        } else {
            campaign.setStatus(CampaignStatus.INACTIVE);
        }

        return theirCampaignRepository.save(campaign);
    }

    // TheirCampaignService.java
    // TheirCampaignService.java
    public TheirCampaign updateCampaign(Long id, TheirCampaignRequest request) {
        TheirCampaign existing = theirCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));


        if (request.getEndDate().isBefore(request.getStartDate().plusDays(1))) {
            throw new IllegalArgumentException("End date must be at least one day after start date");
        }
        System.out.println(existing);
        // Обновляем только разрешенные поля
        existing.setPartnerName(request.getPartnerName());
        existing.setImageUrl(request.getImageUrl());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());

        // Автоматическое обновление статуса
        LocalDate today = LocalDate.now();
        if ((today.isEqual(existing.getStartDate()) || today.isAfter(existing.getStartDate()))
                && (today.isEqual(existing.getEndDate()) || today.isBefore(existing.getEndDate()))) {
            existing.setStatus(CampaignStatus.ACTIVE);
        } else {
            existing.setStatus(CampaignStatus.INACTIVE);
        }
        return theirCampaignRepository.save(existing);


    }


    public void toggleStatus(Long id) {
        TheirCampaign campaign = theirCampaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        CampaignStatus newStatus = campaign.getStatus() == CampaignStatus.ACTIVE
                ? CampaignStatus.INACTIVE
                : CampaignStatus.ACTIVE;

        theirCampaignRepository.updateStatus(id, newStatus);
    }

    public boolean deleteCampaign(Long id) {
        TheirCampaign campaign = theirCampaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (campaign.getStatus() == CampaignStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete active campaign");
        }

        theirCampaignRepository.delete(campaign);
        return true;
    }

}
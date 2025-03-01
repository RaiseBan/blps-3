package com.example.blps.service.data;// TheirCampaignService.java
import com.example.blps.dto.data.TheirCampaignRequest;
import com.example.blps.errorHandler.ConflictException;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.errorHandler.ValidationException;
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

    public TheirCampaign getCampaignById(Long id) {
        return theirCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
    }

    public TheirCampaign createCampaign(TheirCampaignRequest request) {
        TheirCampaign campaign = new TheirCampaign();
        campaign.setPartnerName(request.getPartnerName());
        campaign.setImageUrl(request.getImageUrl());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        updateCampaignStatus(campaign);
        return theirCampaignRepository.save(campaign);
    }

    public TheirCampaign updateCampaign(Long id, TheirCampaignRequest request) {
        TheirCampaign existing = theirCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        validateDates(request);
        updateCampaignFields(existing, request);
        updateCampaignStatus(existing);
        return theirCampaignRepository.save(existing);
    }

    public void toggleStatus(Long id) {
        TheirCampaign campaign = theirCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        CampaignStatus newStatus = campaign.getStatus() == CampaignStatus.ACTIVE
                ? CampaignStatus.INACTIVE
                : CampaignStatus.ACTIVE;

        theirCampaignRepository.updateStatus(id, newStatus);
    }

    public void deleteCampaign(Long id) {
        TheirCampaign campaign = theirCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        if (campaign.getStatus() == CampaignStatus.ACTIVE) {
            throw new ConflictException("Cannot delete active campaign");
        }

        theirCampaignRepository.delete(campaign);
    }

    private void validateDates(TheirCampaignRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate().plusDays(1))) {
            throw new ValidationException("End date must be at least one day after start date");
        }
    }

    private void updateCampaignFields(TheirCampaign existing, TheirCampaignRequest request) {
        existing.setPartnerName(request.getPartnerName());
        existing.setImageUrl(request.getImageUrl());
        existing.setStartDate(request.getStartDate());
        existing.setEndDate(request.getEndDate());
    }

    private void updateCampaignStatus(TheirCampaign campaign) {
        LocalDate today = LocalDate.now();
        CampaignStatus status = ((today.isEqual(campaign.getStartDate()) || today.isAfter(campaign.getStartDate()))
                && (today.isEqual(campaign.getEndDate()) || today.isBefore(campaign.getEndDate())))
                ? CampaignStatus.ACTIVE
                : CampaignStatus.INACTIVE;
        campaign.setStatus(status);
    }
}
package com.example.blps.service.data;

import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.dto.data.OurCampaignRequest;
import com.example.blps.errorHandler.ConflictException;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.controllers.utils.CampaignMapper;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OurCampaignService {

    private final OurCampaignRepository ourCampaignRepository;
    private final CampaignMapper campaignMapper;

    public List<OurCampaignDTO> getAllCampaigns() {
        return ourCampaignRepository.findAll().stream()
                .map(campaignMapper::toDTO)
                .collect(Collectors.toList());
    }

    public OurCampaignDTO getCampaignById(Long id) {
        return ourCampaignRepository.findById(id)
                .map(campaignMapper::toDTO)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
    }

    public OurCampaignDTO createCampaign(OurCampaignRequest request) {
        if (ourCampaignRepository.existsByCampaignName(request.getCampaignName())) {
            throw new ConflictException("Campaign name already exists");
        }

        OurCampaign newCampaign = campaignMapper.toEntity(request);
        
        // Генерируем реферальную ссылку
        String referralLink = generateReferralLink(newCampaign);
        newCampaign.setReferralLink(referralLink);
        
        initializeMetric(newCampaign);

        OurCampaign savedCampaign = ourCampaignRepository.save(newCampaign);
        return campaignMapper.toDTO(savedCampaign);
    }

    public OurCampaignDTO updateCampaign(Long id, OurCampaignRequest request) {
        OurCampaign existingCampaign = ourCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        // Проверяем, изменилось ли имя кампании
        boolean isNameChanged = !existingCampaign.getCampaignName().equals(request.getCampaignName());
        
        updateCampaignFields(existingCampaign, request);
        
        // Если имя кампании изменилось, генерируем новую реферальную ссылку
        if (isNameChanged) {
            String newReferralLink = generateReferralLink(existingCampaign);
            existingCampaign.setReferralLink(newReferralLink);
        }
        
        OurCampaign updatedCampaign = ourCampaignRepository.save(existingCampaign);

        return campaignMapper.toDTO(updatedCampaign);
    }

    public void deleteCampaign(Long id) {
        OurCampaign campaign = ourCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));

        ourCampaignRepository.delete(campaign);
    }

    public Optional<OurCampaign> findByReferralHash(String referralHash) {
        return ourCampaignRepository.findByReferralLink(referralHash);
    }

    private void initializeMetric(OurCampaign campaign) {
        if (campaign.getMetric() == null) {
            Metric metric = new Metric();
            metric.setCampaign(campaign);
            campaign.setMetric(metric);
        }
    }

    private void updateCampaignFields(OurCampaign existing, OurCampaignRequest request) {
        existing.setCampaignName(request.getCampaignName());
        existing.setBudget(request.getBudget());
        existing.setPlacementUrl(request.getPlacementUrl());
    }

    /**
     * Генерирует уникальную реферальную ссылку для кампании
     */
    public String generateReferralLink(OurCampaign campaign) {
        try {
            String base = campaign.getCampaignName() + Instant.now().toEpochMilli();
            String hash = Hashing.sha256()
                    .hashString(base, StandardCharsets.UTF_8)
                    .toString();
            // Возвращаем хэш без полного URL
            return hash.substring(0, 12);
        } catch (Exception e) {
            throw new RuntimeException("Error generating referral link", e);
        }
    }
}
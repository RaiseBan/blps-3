package com.example.blps.service.data;

import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.repository.data.OurCampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final OurCampaignRepository ourCampaignRepository;

    public List<CampaignReportDTO> getCampaignsReportData() {
        return ourCampaignRepository.findAll().stream()
                .map(this::convertToReportDTO)
                .collect(Collectors.toList());
    }

    public CampaignReportDTO getCampaignReportData(Long id) {
        OurCampaign campaign = ourCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
        return convertToReportDTO(campaign);
    }

    private CampaignReportDTO convertToReportDTO(OurCampaign campaign) {
        CampaignReportDTO dto = new CampaignReportDTO();
        dto.setCampaignName(campaign.getCampaignName());
        dto.setBudget(campaign.getBudget());

        Metric metric = campaign.getMetric();
        if (metric != null) {
            dto.setClickCount(metric.getClickCount());
            dto.setCtr(metric.getCtr());
            dto.setConversionRate(metric.getConversionRate());
            dto.setRoi(metric.getRoi());
        }
        return dto;
    }
}
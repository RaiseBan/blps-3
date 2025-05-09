package com.example.blps.service.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import com.example.blps.dto.data.TheirCampaignRequest;
import com.example.blps.errorHandler.ConflictException;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.errorHandler.ValidationException;
import com.example.blps.model.dataEntity.CampaignStatus;
import com.example.blps.model.dataEntity.TheirCampaign;
import com.example.blps.repository.data.TheirCampaignRepository;

@Service
public class TheirCampaignService {

    private final TheirCampaignRepository theirCampaignRepository;
    private final PlatformTransactionManager transactionManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TheirCampaignService(TheirCampaignRepository theirCampaignRepository, PlatformTransactionManager transactionManager) {
        this.theirCampaignRepository = theirCampaignRepository;
        this.transactionManager = transactionManager;
    }

    public List<TheirCampaign> getAllCampaigns() {
        return theirCampaignRepository.findAll();
    }

    public TheirCampaign getCampaignById(Long id) {
        return theirCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
    }

    public TheirCampaign createCampaign(TheirCampaignRequest request) {
        validateDates(request);
        
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
    
    public List<TheirCampaign> importFromFile(MultipartFile file) {
        
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("importFromFileTransaction");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        List<TheirCampaign> importedCampaigns = new ArrayList<>();
        
        try {
            
            List<TheirCampaignRequest> campaignRequests = parseCsvFile(file);
            
            for (TheirCampaignRequest request : campaignRequests) {
                validateDates(request);
                TheirCampaign campaign = createCampaign(request);
                importedCampaigns.add(campaign);
            }
            
            transactionManager.commit(status);
            return importedCampaigns;
        } catch (Exception e) {
            
            transactionManager.rollback(status);
            throw new ValidationException("Ошибка при импорте файла: " + e.getMessage());
        }
    }
    
    private List<TheirCampaignRequest> parseCsvFile(MultipartFile file) throws IOException {
        List<TheirCampaignRequest> campaignRequests = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withHeader("partnerName", "imageUrl", "startDate", "endDate")
                    .withFirstRecordAsHeader()
                    .parse(reader);
            
            for (CSVRecord record : csvParser) {
                TheirCampaignRequest request = new TheirCampaignRequest();
                request.setPartnerName(record.get("partnerName"));
                request.setImageUrl(record.get("imageUrl"));
                request.setStartDate(LocalDate.parse(record.get("startDate"), DATE_FORMATTER));
                request.setEndDate(LocalDate.parse(record.get("endDate"), DATE_FORMATTER));
                
                campaignRequests.add(request);
            }
        }
        
        return campaignRequests;
    }

    private void validateDates(TheirCampaignRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate().plusDays(1))) {
            throw new ValidationException("Дата окончания должна быть не раньше, чем через день после даты начала.");
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

    public void setCampaignsStatus(List<Long> campaignIds, CampaignStatus status) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("setBulkStatusTransaction");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus transactionStatus = transactionManager.getTransaction(definition);

        try {
            for (Long id : campaignIds) {
                if (!theirCampaignRepository.existsById(id)) {
                    throw new NotFoundException("Campaign with ID " + id + " not found");
                }
            }

            for (Long id : campaignIds) {
                theirCampaignRepository.updateStatus(id, status);
            }

            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }
}
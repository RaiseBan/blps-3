package com.example.blps.service.data;

import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.dto.data.OurCampaignRequest;
import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.errorHandler.ConflictException;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.repository.data.OurCampaignRepository;
import com.example.blps.controllers.utils.CampaignMapper;
import com.example.blps.service.notification.MessageSenderService;
import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OurCampaignService {

    private final OurCampaignRepository ourCampaignRepository;
    private final CampaignMapper campaignMapper;
    private final PlatformTransactionManager transactionManager;
    private final MessageSenderService messageSenderService;

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
        
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("createCampaignTransaction");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = transactionManager.getTransaction(definition);

        try {
            if (ourCampaignRepository.existsByCampaignName(request.getCampaignName())) {
                throw new ConflictException("Campaign name already exists");
            }

            OurCampaign newCampaign = campaignMapper.toEntity(request);

            String referralLink = generateReferralLink(newCampaign);
            newCampaign.setReferralLink(referralLink);

            initializeMetric(newCampaign);

            OurCampaign savedCampaign = ourCampaignRepository.save(newCampaign);

            transactionManager.commit(status);

            sendCampaignCreatedNotification(savedCampaign);

            return campaignMapper.toDTO(savedCampaign);
        } catch (Exception e) {
            
            transactionManager.rollback(status);
            log.error("Ошибка при создании кампании: {}", e.getMessage());
            throw e;
        }
    }

    public OurCampaignDTO updateCampaign(Long id, OurCampaignRequest request) {
        
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("updateCampaignTransaction");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = transactionManager.getTransaction(definition);

        try {
            OurCampaign existingCampaign = ourCampaignRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Campaign not found"));

            boolean isNameChanged = !existingCampaign.getCampaignName().equals(request.getCampaignName());

            updateCampaignFields(existingCampaign, request);

            if (isNameChanged) {
                String newReferralLink = generateReferralLink(existingCampaign);
                existingCampaign.setReferralLink(newReferralLink);
            }

            OurCampaign updatedCampaign = ourCampaignRepository.save(existingCampaign);

            transactionManager.commit(status);

            sendCampaignUpdatedNotification(updatedCampaign);

            return campaignMapper.toDTO(updatedCampaign);
        } catch (Exception e) {
            
            transactionManager.rollback(status);
            log.error("Ошибка при обновлении кампании: {}", e.getMessage());
            throw e;
        }
    }

    public void deleteCampaign(Long id) {
        
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("deleteCampaignTransaction");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = transactionManager.getTransaction(definition);

        try {
            OurCampaign campaign = ourCampaignRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Campaign not found"));

            String campaignName = campaign.getCampaignName();

            ourCampaignRepository.delete(campaign);

            transactionManager.commit(status);

            sendCampaignDeletedNotification(id, campaignName);
        } catch (Exception e) {
            
            transactionManager.rollback(status);
            log.error("Ошибка при удалении кампании: {}", e.getMessage());
            throw e;
        }
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

    public String generateReferralLink(OurCampaign campaign) {
        try {
            String base = campaign.getCampaignName() + Instant.now().toEpochMilli();
            String hash = Hashing.sha256()
                    .hashString(base, StandardCharsets.UTF_8)
                    .toString();
            
            return hash.substring(0, 12);
        } catch (Exception e) {
            throw new RuntimeException("Error generating referral link", e);
        }
    }

    private void sendCampaignCreatedNotification(OurCampaign campaign) {
        try {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "system";

            NotificationMessage notification = NotificationMessage.builder()
                    .title("Создана новая кампания: " + campaign.getCampaignName())
                    .message(String.format(
                            "Создана новая рекламная кампания:\n" +
                                    "Название: %s\n" +
                                    "Бюджет: %s\n" +
                                    "Реферальная ссылка: %s\n" +
                                    "Создана пользователем: %s",
                            campaign.getCampaignName(),
                            campaign.getBudget(),
                            campaign.getReferralLink(),
                            username
                    ))
                    .type(NotificationType.CAMPAIGN_CREATED)
                    .recipient("ROLE_ADMIN,ROLE_CAMPAIGN_MANAGER,ROLE_ANALYST")
                    .relatedEntityId(campaign.getId())
                    .build();

            messageSenderService.sendNotification(notification);
        } catch (Exception e) {
            
            log.error("Ошибка при отправке уведомления о создании кампании: {}", e.getMessage());
        }
    }

    private void sendCampaignUpdatedNotification(OurCampaign campaign) {
        try {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "system";

            NotificationMessage notification = NotificationMessage.builder()
                    .title("Обновлена кампания: " + campaign.getCampaignName())
                    .message(String.format(
                            "Обновлена рекламная кампания:\n" +
                                    "Название: %s\n" +
                                    "Бюджет: %s\n" +
                                    "Обновлена пользователем: %s",
                            campaign.getCampaignName(),
                            campaign.getBudget(),
                            username
                    ))
                    .type(NotificationType.CAMPAIGN_UPDATED)
                    .recipient("ROLE_ADMIN,ROLE_CAMPAIGN_MANAGER,ROLE_ANALYST")
                    .relatedEntityId(campaign.getId())
                    .build();

            messageSenderService.sendNotification(notification);
        } catch (Exception e) {
            
            log.error("Ошибка при отправке уведомления об обновлении кампании: {}", e.getMessage());
        }
    }

    private void sendCampaignDeletedNotification(Long campaignId, String campaignName) {
        try {
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "system";

            NotificationMessage notification = NotificationMessage.builder()
                    .title("Удалена кампания: " + campaignName)
                    .message(String.format(
                            "Удалена рекламная кампания:\n" +
                                    "Название: %s\n" +
                                    "ID: %d\n" +
                                    "Удалена пользователем: %s",
                            campaignName,
                            campaignId,
                            username
                    ))
                    .type(NotificationType.CAMPAIGN_DELETED)
                    .recipient("ROLE_ADMIN,ROLE_CAMPAIGN_MANAGER")
                    .build();

            messageSenderService.sendNotification(notification);
        } catch (Exception e) {
            
            log.error("Ошибка при отправке уведомления об удалении кампании: {}", e.getMessage());
        }
    }
}
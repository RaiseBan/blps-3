package com.example.blps.controllers;

import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.service.data.ReferralService;
import com.example.blps.service.geo.GeoLocationProcessingService;
import com.example.blps.service.notification.MessageSenderService;
import com.example.blps.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/redirect")
@RequiredArgsConstructor
@Slf4j
public class RedirectController {
    private final ReferralService referralService;
    private final MessageSenderService messageSenderService;

    @GetMapping("/{referralHash}")
    public RedirectView handleRedirect(@PathVariable String referralHash, HttpServletRequest request) {
        try {
            // Обрабатываем клик как и раньше
            OurCampaign campaign = referralService.processReferralClick(referralHash);

            // Извлекаем IP адрес
            String clientIp = IpUtils.getClientIpAddress(request);
            log.info("Redirect request from IP: {} for referral: {}", clientIp, referralHash);

            // Отправляем запрос на асинхронную обработку геолокации
            GeoLocationProcessingService.GeoLocationRequest geoRequest =
                    GeoLocationProcessingService.GeoLocationRequest.builder()
                            .ip(clientIp)
                            .referralHash(referralHash)
                            .campaignId(campaign.getId())
                            .build();

            messageSenderService.sendMessage(GeoLocationProcessingService.GEO_PROCESSING_QUEUE, geoRequest);

        } catch (Exception e) {
            log.error("Error processing redirect for referral: {}", referralHash, e);
        }

        // Всегда возвращаем на главную страницу
        return new RedirectView("/");
    }
}
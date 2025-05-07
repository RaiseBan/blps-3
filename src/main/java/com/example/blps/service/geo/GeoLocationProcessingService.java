package com.example.blps.service.geo;

import com.example.blps.components.GeoLocationStorage;
import com.example.blps.model.geo.GeoLocationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

/**
 * Сервис для асинхронной обработки геолокационных запросов
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationProcessingService {
    
    private final GeoLocationService geoLocationService;
    private final GeoLocationStorage geoLocationStorage;
    
    public static final String GEO_PROCESSING_QUEUE = "geo.processing.queue";
    
    /**
     * Обрабатывает запрос на определение геолокации из очереди
     */
    @JmsListener(destination = GEO_PROCESSING_QUEUE)
    public void processGeoLocationRequest(GeoLocationRequest request) {
        log.debug("Processing geo location request for IP: {}", request.getIp());
        
        try {
            GeoLocationData geoData = geoLocationService.getGeoData(
                    request.getIp(), 
                    request.getReferralHash(), 
                    request.getCampaignId()
            );
            
            geoLocationStorage.addOrUpdateGeoData(request.getIp(), geoData);
            
        } catch (Exception e) {
            log.error("Error processing geo location request for IP: {}", request.getIp(), e);
        }
    }
    
    /**
     * DTO для запроса на обработку геолокации
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GeoLocationRequest implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private String ip;
        private String referralHash;
        private Long campaignId;
    }
}
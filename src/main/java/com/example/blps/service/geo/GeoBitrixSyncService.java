package com.example.blps.service.geo;

import com.example.blps.components.GeoLocationStorage;
import com.example.blps.model.geo.GeoLocationData;
import com.example.blps.service.integration.Bitrix24Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.resource.ResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoBitrixSyncService {
    
    private final GeoLocationStorage geoLocationStorage;
    private final Bitrix24Service bitrix24Service;
    private final ObjectMapper objectMapper;
    
    public void syncGeoDataToBitrix() {
        List<GeoLocationData> geoDataList = geoLocationStorage.getAndClearAll();
        
        if (geoDataList.isEmpty()) {
            log.debug("No geo data to sync with Bitrix24");
            return;
        }
        
        log.info("Syncing {} geo data records to Bitrix24", geoDataList.size());
        
        try {
            
            Map<String, Long> countryStats = geoDataList.stream()
                    .collect(Collectors.groupingBy(
                            GeoLocationData::getCountry,
                            Collectors.summingLong(data -> data.getClickCount().longValue())
                    ));
            
            Map<Long, List<GeoLocationData>> byCampaign = geoDataList.stream()
                    .filter(data -> data.getCampaignId() != null)
                    .collect(Collectors.groupingBy(GeoLocationData::getCampaignId));
            
            String report = createGeoReport(geoDataList, countryStats, byCampaign);
            
            String title = "Геолокационный отчет - " + LocalDateTime.now();
            Map<String, Object> params = new HashMap<>();
            params.put("fields[TITLE]", title);
            params.put("fields[COMMENTS]", report);
            params.put("fields[ASSIGNED_BY_ID]", "1");
            params.put("fields[SOURCE_ID]", "ADVERTISING");
            String taskId = bitrix24Service.executeAbstractMethod("crm.lead.add", params);
            
            log.info("Geo data successfully synced to Bitrix24, task ID: {}", taskId);
            
        } catch (ResourceException e) {
            log.error("Error syncing geo data to Bitrix24", e);
            
            geoDataList.forEach(data -> geoLocationStorage.addOrUpdateGeoData(data.getIp(), data));
        }
    }
    
    private String createGeoReport(List<GeoLocationData> geoDataList, 
                                   Map<String, Long> countryStats,
                                   Map<Long, List<GeoLocationData>> byCampaign) {
        StringBuilder report = new StringBuilder();
        
        report.append("=== ОТЧЕТ ПО ГЕОЛОКАЦИИ ===\n\n");
        report.append("Время генерации: ").append(LocalDateTime.now()).append("\n");
        report.append("Всего записей: ").append(geoDataList.size()).append("\n");
        report.append("Всего кликов: ").append(
                geoDataList.stream().mapToInt(GeoLocationData::getClickCount).sum()
        ).append("\n\n");
        
        report.append("=== СТАТИСТИКА ПО СТРАНАМ ===\n");
        countryStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> report.append(String.format("%s: %d кликов\n", 
                        entry.getKey(), entry.getValue())));
        
        report.append("\n=== СТАТИСТИКА ПО КАМПАНИЯМ ===\n");
        byCampaign.forEach((campaignId, dataList) -> {
            report.append(String.format("\nКампания ID %d:\n", campaignId));
            
            Map<String, Long> campaignCountryStats = dataList.stream()
                    .collect(Collectors.groupingBy(
                            GeoLocationData::getCountry,
                            Collectors.summingLong(data -> data.getClickCount().longValue())
                    ));
            
            campaignCountryStats.forEach((country, clicks) -> 
                    report.append(String.format("  - %s: %d кликов\n", country, clicks)));
        });
        
        report.append("\n=== ДЕТАЛЬНАЯ ИНФОРМАЦИЯ ===\n");
        
        geoDataList.stream().limit(10).forEach(data -> {
            report.append(String.format("\nIP: %s\n", data.getIp()));
            report.append(String.format("Страна: %s (%s)\n", data.getCountry(), data.getCountryCode()));
            report.append(String.format("Регион: %s\n", data.getRegion()));
            report.append(String.format("Город: %s\n", data.getCity()));
            report.append(String.format("Кликов: %d\n", data.getClickCount()));
            report.append(String.format("Время: %s\n", data.getTimestamp()));
        });
        
        if (geoDataList.size() > 10) {
            report.append("\n... и еще ").append(geoDataList.size() - 10).append(" записей\n");
        }
        
        return report.toString();
    }
}
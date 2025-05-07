package com.example.blps.components;

import com.example.blps.model.geo.GeoLocationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Потокобезопасное хранилище для геолокационных данных
 */
@Component
@Slf4j
public class GeoLocationStorage {
    
    // Храним данные по IP адресам
    private final Map<String, GeoLocationData> geoDataMap = new ConcurrentHashMap<>();
    
    /**
     * Добавляет или обновляет геолокационные данные
     */
    public void addOrUpdateGeoData(String ip, GeoLocationData data) {
        GeoLocationData existingData = geoDataMap.get(ip);
        
        if (existingData != null) {
            // Если данные уже есть, увеличиваем счетчик кликов
            existingData.setClickCount(existingData.getClickCount() + 1);
            existingData.setTimestamp(data.getTimestamp());
            log.debug("Updated geo data for IP: {}, click count: {}", ip, existingData.getClickCount());
        } else {
            // Новые данные
            geoDataMap.put(ip, data);
            log.debug("Added new geo data for IP: {}", ip);
        }
    }
    
    /**
     * Получает все данные и очищает хранилище
     */
    public List<GeoLocationData> getAndClearAll() {
        List<GeoLocationData> result = new ArrayList<>(geoDataMap.values());
        geoDataMap.clear();
        log.info("Retrieved and cleared {} geo data records", result.size());
        return result;
    }
    
    /**
     * Получает количество записей в хранилище
     */
    public int size() {
        return geoDataMap.size();
    }
    
    /**
     * Проверяет, есть ли данные для IP
     */
    public boolean containsIp(String ip) {
        return geoDataMap.containsKey(ip);
    }
}
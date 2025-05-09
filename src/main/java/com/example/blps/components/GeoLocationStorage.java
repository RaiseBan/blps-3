package com.example.blps.components;

import com.example.blps.model.geo.GeoLocationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class GeoLocationStorage {
    
    private final Map<String, GeoLocationData> geoDataMap = new ConcurrentHashMap<>();
    
    public void addOrUpdateGeoData(String ip, GeoLocationData data) {
        GeoLocationData existingData = geoDataMap.get(ip);
        
        if (existingData != null) {
            
            existingData.setClickCount(existingData.getClickCount() + 1);
            existingData.setTimestamp(data.getTimestamp());
            log.debug("Updated geo data for IP: {}, click count: {}", ip, existingData.getClickCount());
        } else {
            
            geoDataMap.put(ip, data);
            log.debug("Added new geo data for IP: {}", ip);
        }
    }
    
    public List<GeoLocationData> getAndClearAll() {
        List<GeoLocationData> result = new ArrayList<>(geoDataMap.values());
        geoDataMap.clear();
        log.info("Retrieved and cleared {} geo data records", result.size());
        return result;
    }
    
    public int size() {
        return geoDataMap.size();
    }
    
    public boolean containsIp(String ip) {
        return geoDataMap.containsKey(ip);
    }
}
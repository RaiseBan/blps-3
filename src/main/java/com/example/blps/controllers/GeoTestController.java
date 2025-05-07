package com.example.blps.controllers;

import com.example.blps.components.GeoLocationStorage;
import com.example.blps.model.geo.GeoLocationData;
import com.example.blps.service.geo.GeoBitrixSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/geo-test")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GeoTestController {
    
    private final GeoLocationStorage geoLocationStorage;
    private final GeoBitrixSyncService geoBitrixSyncService;
    
    /**
     * Показывает текущее состояние гео-хранилища
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGeoStorageStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("recordsCount", geoLocationStorage.size());
        status.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(status);
    }
    
    /**
     * Запускает ручную синхронизацию с Bitrix24
     */
    @PostMapping("/sync")
    public ResponseEntity<String> triggerGeoSync() {
        geoBitrixSyncService.syncGeoDataToBitrix();
        return ResponseEntity.ok("Geo data sync triggered manually");
    }
}
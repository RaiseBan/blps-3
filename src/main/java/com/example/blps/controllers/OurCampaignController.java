package com.example.blps.controllers;


import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.dto.data.OurCampaignRequest;
import com.example.blps.service.data.OurCampaignService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/our-campaigns")
public class OurCampaignController {

    private final OurCampaignService campaignService;

    public OurCampaignController(OurCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public ResponseEntity<List<OurCampaignDTO>> getAll() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OurCampaignDTO> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(campaignService.getCampaignById(id)
                    .orElseThrow(() -> new RuntimeException("Campaign not found")));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody OurCampaignRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(campaignService.createCampaign(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OurCampaignDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody OurCampaignRequest request) {
        try {
            return ResponseEntity.ok(campaignService.updateCampaign(id, request));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            campaignService.deleteCampaign(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
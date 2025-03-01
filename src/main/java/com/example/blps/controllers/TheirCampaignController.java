package com.example.blps.controllers;// TheirCampaignController.java
import com.example.blps.dto.data.TheirCampaignRequest;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.TheirCampaign;
import com.example.blps.service.data.TheirCampaignService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/their-campaigns")
public class TheirCampaignController {

    private final TheirCampaignService campaignService;

    public TheirCampaignController(TheirCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public ResponseEntity<List<TheirCampaign>> getAll() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheirCampaign> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignById(id));
    }

    @PostMapping
    public ResponseEntity<TheirCampaign> create(@Valid @RequestBody TheirCampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TheirCampaign> update(
            @PathVariable Long id,
            @Valid @RequestBody TheirCampaignRequest request) {
        return ResponseEntity.ok(campaignService.updateCampaign(id, request));
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        campaignService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}
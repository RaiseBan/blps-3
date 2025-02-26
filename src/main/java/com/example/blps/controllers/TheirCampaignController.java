package com.example.blps.controllers;// TheirCampaignController.java
import com.example.blps.model.dataEntity.TheirCampaign;
import com.example.blps.service.data.TheirCampaignService;
import jakarta.validation.Valid;
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
        return campaignService.getCampaignById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TheirCampaign> create(@Valid @RequestBody TheirCampaign campaign) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.createCampaign(campaign));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TheirCampaign> update(
            @PathVariable Long id,
            @Valid @RequestBody TheirCampaign campaign
    ) {
        return campaignService.updateCampaign(id, campaign)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return campaignService.deleteCampaign(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
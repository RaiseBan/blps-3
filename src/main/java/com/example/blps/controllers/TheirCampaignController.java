package com.example.blps.controllers;

import java.util.List;

import com.example.blps.dto.data.SetStatusRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.blps.dto.data.TheirCampaignRequest;
import com.example.blps.model.dataEntity.TheirCampaign;
import com.example.blps.service.data.TheirCampaignService;

import jakarta.validation.Valid;

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

    @PostMapping("/import")
    public ResponseEntity<List<TheirCampaign>> importFromFile(@RequestParam("file") MultipartFile file) {
        List<TheirCampaign> importedCampaigns = campaignService.importFromFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(importedCampaigns);
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
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/set-status")
    public ResponseEntity<Void> setCampaignsStatus(
            @RequestBody SetStatusRequest request) {
        campaignService.setCampaignsStatus(request.getCampaignIds(), request.getStatus());
        return ResponseEntity.ok().build();
    }
}
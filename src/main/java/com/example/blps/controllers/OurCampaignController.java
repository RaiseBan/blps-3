package com.example.blps.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.blps.dto.data.OurCampaignDTO;
import com.example.blps.dto.data.OurCampaignRequest;
import com.example.blps.service.data.BudgetOptimizationService;
import com.example.blps.service.data.OurCampaignService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/our-campaigns")
public class OurCampaignController {

    private final OurCampaignService campaignService;
    private final BudgetOptimizationService budgetOptimizationService;

    public OurCampaignController(OurCampaignService campaignService,
            BudgetOptimizationService budgetOptimizationService) {
        this.campaignService = campaignService;
        this.budgetOptimizationService = budgetOptimizationService;
    }

    @GetMapping
    public ResponseEntity<List<OurCampaignDTO>> getAll() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OurCampaignDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignById(id));
    }

    @PostMapping
    public ResponseEntity<OurCampaignDTO> create(@Valid @RequestBody OurCampaignRequest request) {
        OurCampaignDTO createdCampaign = campaignService.createCampaign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCampaign);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OurCampaignDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody OurCampaignRequest request) {
        return ResponseEntity.ok(campaignService.updateCampaign(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/optimize-budget")
    public ResponseEntity<OurCampaignDTO> optimizeBudget(@PathVariable Long id) {
        OurCampaignDTO optimizedCampaign = budgetOptimizationService.optimizeCampaignBudget(id);
        return ResponseEntity.ok(optimizedCampaign);
    }
}
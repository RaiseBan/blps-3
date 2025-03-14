package com.example.blps.controllers;

import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.model.dataEntity.Metric;
import com.example.blps.model.dataEntity.OurCampaign;
import com.example.blps.service.data.MetricService;
import com.example.blps.service.data.OurCampaignService;
import com.example.blps.service.data.ReferralService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/redirect")
@RequiredArgsConstructor
public class RedirectController {
    private final ReferralService referralService;

    @GetMapping("/{referralHash}")
    public RedirectView handleRedirect(@PathVariable String referralHash) {
        referralService.processReferralClick(referralHash);
        return new RedirectView("/");
    }
}
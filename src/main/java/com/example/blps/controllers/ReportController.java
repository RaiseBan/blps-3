package com.example.blps.controllers;

import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.service.data.CsvReportService;
import com.example.blps.service.data.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final CsvReportService csvReportService;

    @GetMapping("/api/reports/campaigns")
    public ResponseEntity<byte[]> generateCampaignsReport() {
        return ResponseEntity.ok()
                .headers(buildCsvHeaders("campaigns-report.csv"))
                .body(csvReportService.generateCampaignsReport(reportService.getCampaignsReportData()));
    }

    @GetMapping("/api/reports/campaigns/{id}")
    public ResponseEntity<byte[]> generateCampaignReportById(@PathVariable Long id) {
        CampaignReportDTO dto = reportService.getCampaignReportData(id);
        return ResponseEntity.ok()
                .headers(buildCsvHeaders("campaign-" + id + "-report.csv"))
                .body(csvReportService.generateCampaignReport(dto));
    }

    private HttpHeaders buildCsvHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        return headers;
    }
}
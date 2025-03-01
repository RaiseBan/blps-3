package com.example.blps.controllers;// ReportController.java
import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.errorHandler.NotFoundException;
import com.example.blps.service.data.OurCampaignService;
import com.example.blps.service.data.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/api/reports/campaigns")
    public void generateCampaignsReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"campaigns-report.csv\""
        );

        List<CampaignReportDTO> reportData = reportService.getCampaignsReportData();

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Campaign Name,Budget,Click Count,CTR,Conversion Rate,ROI");
            for (CampaignReportDTO dto : reportData) {
                writer.println(formatCsvLine(dto));
            }
        }
    }

    @GetMapping("/api/reports/campaigns/{id}")
    public void generateCampaignReportById(
            @PathVariable Long id,
            HttpServletResponse response
    ) throws IOException {
        CampaignReportDTO dto = reportService.getCampaignReportData(id);

        response.setContentType("text/csv");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"campaign-" + id + "-report.csv\""
        );

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Campaign Name,Budget,Click Count,CTR,Conversion Rate,ROI");
            writer.println(formatCsvLine(dto));
        }
    }

    private String formatCsvLine(CampaignReportDTO dto) {
        return String.format(Locale.US, "\"%s\",%.2f,%d,%.2f,%.2f,%.2f",
                dto.getCampaignName(),
                dto.getBudget(),
                dto.getClickCount(),
                dto.getCtr(),
                dto.getConversionRate(),
                dto.getRoi());
    }
}
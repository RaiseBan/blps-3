package com.example.blps.controllers;// ReportController.java
import com.example.blps.dto.data.CampaignReportDTO;
import com.example.blps.service.data.OurCampaignService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
public class ReportController {

    private final OurCampaignService campaignService;

    public ReportController(OurCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping("/api/reports/campaigns")
    public void generateCampaignsReport(HttpServletResponse response) throws IOException {
        // Настройка заголовков ответа
        response.setContentType("text/csv");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"campaigns-report.csv\""
        );

        // Получение данных
        List<CampaignReportDTO> reportData = campaignService.getCampaignsReportData();

        // Генерация CSV
        try (PrintWriter writer = response.getWriter()) {
            // Заголовки CSV
            writer.println("Campaign Name,Budget,Click Count,CTR,Conversion Rate,ROI");

            // Данные
            for (CampaignReportDTO dto : reportData) {
                writer.println(String.format(Locale.US, "\"%s\",%.2f,%d,%.2f,%.2f,%.2f",
                        dto.getCampaignName(),
                        dto.getBudget(),
                        dto.getClickCount(),
                        dto.getCtr(),
                        dto.getConversionRate(),
                        dto.getRoi()
                ));

            }
        }
    }

    @GetMapping("/api/reports/campaigns/{id}")
    public void generateCampaignReportById(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Optional<CampaignReportDTO> campaignReportOpt = campaignService.getCampaignReportData(id);
        if (campaignReportOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Campaign not found");
            return;
        }
        CampaignReportDTO dto = campaignReportOpt.get();

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"campaign-" + id + "-report.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Campaign Name,Budget,Click Count,CTR,Conversion Rate,ROI");
            writer.println(String.format(Locale.US, "\"%s\",%.2f,%d,%.2f,%.2f,%.2f",
                    dto.getCampaignName(),
                    dto.getBudget(),
                    dto.getClickCount(),
                    dto.getCtr(),
                    dto.getConversionRate(),
                    dto.getRoi()
            ));

        }
    }



}
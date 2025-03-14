package com.example.blps.service.data;

import com.example.blps.dto.data.CampaignReportDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Locale;

@Service
public class CsvReportService {
    
    private static final String CSV_HEADER = "Campaign Name,Budget,Click Count,CTR,Conversion Rate,ROI\n";

    public byte[] generateCampaignsReport(List<CampaignReportDTO> data) {
        StringBuilder csv = new StringBuilder(CSV_HEADER);
        data.forEach(dto -> csv.append(formatCsvLine(dto)).append("\n"));
        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] generateCampaignReport(CampaignReportDTO dto) {
        String csv = CSV_HEADER + formatCsvLine(dto);
        return csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String formatCsvLine(CampaignReportDTO dto) {
        return String.format(Locale.US, "\"%s\",%.2f,%d,%.2f,%.2f,%.2f",
                escapeCsvField(dto.getCampaignName()),
                dto.getBudget(),
                dto.getClickCount(),
                dto.getCtr(),
                dto.getConversionRate(),
                dto.getRoi());
    }

    private String escapeCsvField(String field) {
        return field.replace("\"", "\"\"");
    }
}
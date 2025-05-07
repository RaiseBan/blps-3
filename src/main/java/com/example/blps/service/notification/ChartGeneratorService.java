// src/main/java/com/example/blps/service/notification/ChartGeneratorService.java
package com.example.blps.service.notification;

import com.example.blps.dto.data.CampaignReportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartGeneratorService {

    /**
     * Генерирует столбчатую диаграмму эффективности кампаний в формате base64
     */
    public String generateCampaignPerformanceChart(List<CampaignReportDTO> reports) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CampaignReportDTO report : reports) {
            dataset.addValue(report.getClickCount(), "Клики", report.getCampaignName());
            dataset.addValue(report.getCtr().doubleValue(), "CTR %", report.getCampaignName());
            dataset.addValue(report.getConversionRate().doubleValue(), "Конверсия %", report.getCampaignName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Эффективность кампаний",
                "Кампании",
                "Значения",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return chartToBase64(chart);
    }

    /**
     * Генерирует круговую диаграмму распределения бюджета в формате base64
     */
    public String generateBudgetDistributionChart(List<CampaignReportDTO> reports) throws IOException {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for (CampaignReportDTO report : reports) {
            dataset.setValue(report.getCampaignName(), report.getBudget().doubleValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Распределение бюджета",
                dataset,
                true,
                true,
                false
        );

        return chartToBase64(chart);
    }

    /**
     * Генерирует линейный график ROI в формате base64
     */
    public String generateRoiChart(List<CampaignReportDTO> reports) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CampaignReportDTO report : reports) {
            dataset.addValue(report.getRoi().doubleValue(), "ROI", report.getCampaignName());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "ROI по кампаниям",
                "Кампании",
                "ROI %",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        return chartToBase64(chart);
    }

    private String chartToBase64(JFreeChart chart) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtilities.writeChartAsPNG(baos, chart, 800, 600);
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
}
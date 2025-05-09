package com.example.blps.service.notification;

import com.example.blps.dto.data.CampaignReportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartGeneratorService {

    public String generateAnalyticsReport(List<CampaignReportDTO> reports) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CampaignReportDTO report : reports) {
            String campaignName = truncateName(report.getCampaignName());
            dataset.addValue(report.getClickCount(), "Клики", campaignName);
            dataset.addValue(report.getCtr().doubleValue() * 10, "CTR x10", campaignName);
            dataset.addValue(report.getConversionRate().doubleValue() * 100, "Конверсия %", campaignName);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Аналитика эффективности кампаний",
                "Кампании",
                "Значения",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        return chartToBase64(chart, 1000, 600);
    }

    public String generateFinancialReport(List<CampaignReportDTO> reports) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (CampaignReportDTO report : reports) {
            String campaignName = truncateName(report.getCampaignName());
            dataset.addValue(report.getBudget().doubleValue() / 1000, "Бюджет (тыс.)", campaignName);

            double roi = report.getRoi().doubleValue();
            dataset.addValue(roi + 50, "ROI % (+50)", campaignName);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Финансовые показатели кампаний",
                "Кампании",
                "Значения",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        customizeChart(chart);

        return chartToBase64(chart, 1000, 600);
    }

    public String generateBudgetPieChart(List<CampaignReportDTO> reports) throws IOException {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for (CampaignReportDTO report : reports) {
            String campaignName = truncateName(report.getCampaignName());
            dataset.setValue(campaignName, report.getBudget().doubleValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Распределение бюджета по кампаниям",
                dataset,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));

        return chartToBase64(chart, 800, 600);
    }

    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        renderer.setSeriesPaint(0, new Color(52, 152, 219)); 
        renderer.setSeriesPaint(1, new Color(46, 204, 113)); 
        renderer.setSeriesPaint(2, new Color(231, 76, 60));  
    }

    private String truncateName(String name) {
        return name.length() > 20 ? name.substring(0, 17) + "..." : name;
    }

    private String chartToBase64(JFreeChart chart, int width, int height) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtilities.writeChartAsPNG(baos, chart, width, height);
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
}
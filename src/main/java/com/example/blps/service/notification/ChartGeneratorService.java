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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChartGeneratorService {

    /**
     * Генерирует столбчатую диаграмму эффективности кампаний
     */
    public byte[] generateCampaignPerformanceChart(List<CampaignReportDTO> reports) throws IOException {
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
        
        return chartToBytes(chart);
    }
    
    /**
     * Генерирует круговую диаграмму распределения бюджета
     */
    public byte[] generateBudgetDistributionChart(List<CampaignReportDTO> reports) throws IOException {
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
        
        return chartToBytes(chart);
    }
    
    /**
     * Генерирует линейный график ROI
     */
    public byte[] generateRoiChart(List<CampaignReportDTO> reports) throws IOException {
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
        
        return chartToBytes(chart);
    }
    
    private byte[] chartToBytes(JFreeChart chart) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtilities.writeChartAsPNG(baos, chart, 800, 600);
        return baos.toByteArray();
    }
}
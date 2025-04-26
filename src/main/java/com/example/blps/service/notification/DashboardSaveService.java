package com.example.blps.service.notification;

import com.example.blps.model.notification.Dashboard;
import com.example.blps.model.notification.DashboardType;
import com.example.blps.repository.notification.DashboardRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Сервис для сохранения дашбордов, полученных с другого узла
 * Использует профиль "node1" для запуска только на первом узле
 */
@Service
@RequiredArgsConstructor
@Slf4j
//@Profile("node1")
public class DashboardSaveService {

    private final DashboardRepository dashboardRepository;
    private final ObjectMapper objectMapper;

    /**
     * Сохраняет дашборд, полученный из другого узла
     *
     * @param dashboardData данные дашборда
     */
    @JmsListener(destination = "dashboard.save.queue")
    @Transactional
    public void saveDashboard(Map<String, Object> dashboardData) {
        log.info("Received dashboard data for saving: {}", dashboardData);

        try {
            // Создаем новый объект дашборда
            Dashboard dashboard = new Dashboard();
            dashboard.setTitle((String) dashboardData.get("title"));
            dashboard.setType(DashboardType.valueOf((String) dashboardData.get("type")));
            dashboard.setDescription("Дашборд создан узлом: " + dashboardData.get("nodeId"));

            // Преобразуем данные графика в JSON строку
            Object chartData = dashboardData.get("chartData");
            Object chartConfig = dashboardData.get("chartConfig");

            dashboard.setChartData(objectMapper.writeValueAsString(chartData));
            dashboard.setChartConfig(objectMapper.writeValueAsString(chartConfig));

            // Устанавливаем дату создания
            dashboard.setCreatedAt(LocalDateTime.now());

            // По умолчанию дашборд не опубликован
            dashboard.setIsPublished(false);

            // Сохраняем дашборд
            Dashboard savedDashboard = dashboardRepository.save(dashboard);
            log.info("Dashboard saved successfully with ID: {}", savedDashboard.getId());

        } catch (Exception e) {
            log.error("Error saving dashboard", e);
            throw new RuntimeException("Failed to save dashboard", e);
        }
    }

    /**
     * Сохраняет дашборд и возвращает сохраненную сущность
     *
     * @param dashboard дашборд для сохранения
     * @return сохраненный дашборд с установленным ID
     */
    @JmsListener(destination = "dashboard.save.queue")
    @Transactional
    public Dashboard saveDashboardEntity(Dashboard dashboard) {
        try {
            Dashboard savedDashboard = dashboardRepository.save(dashboard);
            log.info("Dashboard explicitly saved with ID: {}", savedDashboard.getId());
            return savedDashboard;
        } catch (Exception e) {
            log.error("Error explicitly saving dashboard", e);
            throw new RuntimeException("Failed to explicitly save dashboard", e);
        }
    }
}
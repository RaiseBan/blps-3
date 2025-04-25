package com.example.blps.controllers;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.model.notification.Dashboard;
import com.example.blps.model.notification.DashboardType;
import com.example.blps.model.notification.Notification;
import com.example.blps.repository.notification.DashboardRepository;
import com.example.blps.service.notification.MessageSenderService;
import com.example.blps.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final DashboardRepository dashboardRepository;
    private final MessageSenderService messageSenderService;

    /**
     * Получение списка уведомлений для текущего пользователя
     * 
     * @return список уведомлений
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userRole = "ROLE_" + auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        
        List<Notification> notifications = notificationService.getNotificationsForUser(userRole);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Получение списка непрочитанных уведомлений для текущего пользователя
     * 
     * @return список непрочитанных уведомлений
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getMyUnreadNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userRole = "ROLE_" + auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userRole);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Пометка уведомления как прочитанного
     * 
     * @param id идентификатор уведомления
     * @return статус операции
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userRole = "ROLE_" + auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        
        boolean success = notificationService.markAsRead(id, userRole);
        if (success) {
            return ResponseEntity.ok("Notification marked as read");
        } else {
            return ResponseEntity.badRequest().body("Failed to mark notification as read");
        }
    }

    /**
     * Получение списка дашбордов
     * 
     * @return список дашбордов
     */
    @GetMapping("/dashboards")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<Dashboard>> getAllDashboards() {
        List<Dashboard> dashboards = dashboardRepository.findAll();
        return ResponseEntity.ok(dashboards);
    }

    /**
     * Получение опубликованных дашбордов
     * 
     * @return список опубликованных дашбордов
     */
    @GetMapping("/dashboards/published")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Dashboard>> getPublishedDashboards() {
        List<Dashboard> dashboards = dashboardRepository.findByIsPublishedTrueOrderByCreatedAtDesc();
        return ResponseEntity.ok(dashboards);
    }

    /**
     * Получение дашборда по идентификатору
     * 
     * @param id идентификатор дашборда
     * @return дашборд
     */
    @GetMapping("/dashboards/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Dashboard> getDashboardById(@PathVariable Long id) {
        Optional<Dashboard> dashboard = dashboardRepository.findById(id);
        return dashboard.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Запуск задания на генерацию дашборда
     * 
     * @param request запрос на генерацию дашборда
     * @return статус операции
     */
    @PostMapping("/dashboards/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<String> generateDashboard(@RequestBody DashboardGenerationRequest request) {
        // Устанавливаем информацию о создателе
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        request.setCreatedBy(auth.getName());
        
        messageSenderService.sendDashboardGenerationRequest(request);
        return ResponseEntity.ok("Dashboard generation started");
    }

    /**
     * Публикация дашборда
     * 
     * @param id идентификатор дашборда
     * @return статус операции
     */
    @PostMapping("/dashboards/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<String> publishDashboard(@PathVariable Long id) {
        Optional<Dashboard> dashboardOpt = dashboardRepository.findById(id);
        if (dashboardOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Dashboard dashboard = dashboardOpt.get();
        dashboard.setIsPublished(true);
        dashboardRepository.save(dashboard);
        
        return ResponseEntity.ok("Dashboard published successfully");
    }
}
package com.example.blps.service.notification;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSenderService {

    private final StompMessageService stompMessageService;

    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String DASHBOARD_GENERATION_QUEUE = "dashboard.generation.queue";

    public void sendNotification(NotificationMessage notification) {
        log.info("Sending notification message to queue: {}", notification);
        try {
            stompMessageService.sendMessage(NOTIFICATION_QUEUE, notification);
            log.info("Notification message sent successfully via STOMP");
        } catch (Exception e) {
            log.error("Error sending notification message", e);
            throw new RuntimeException("Failed to send notification message", e);
        }
    }

    public void sendDashboardGenerationRequest(DashboardGenerationRequest request) {
        log.info("Sending dashboard generation request to queue: {}", request);
        try {
            
            Map<String, String> headers = new HashMap<>();
            headers.put("dashboardType", request.getType().name());
            headers.put("messageId", UUID.randomUUID().toString());

            stompMessageService.sendMessage(DASHBOARD_GENERATION_QUEUE, request, headers);
            log.info("Dashboard generation request sent successfully via STOMP");
        } catch (Exception e) {
            log.error("Error sending dashboard generation request", e);
            throw new RuntimeException("Failed to send dashboard generation request", e);
        }
    }

    public void sendMessage(String destination, Object message) {
        log.info("Sending message to queue {}: {}", destination, message);
        try {
            stompMessageService.sendMessage(destination, message);
            log.info("Message sent successfully to {} via STOMP", destination);
        } catch (Exception e) {
            log.error("Error sending message to queue " + destination, e);
            throw new RuntimeException("Failed to send message to " + destination, e);
        }
    }
}
package com.example.blps.service.notification;

import com.example.blps.dto.notification.DashboardGenerationRequest;
import com.example.blps.dto.notification.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSenderService {

    private final JmsTemplate jmsTemplate;
    
    // Константы для имен очередей
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String DASHBOARD_GENERATION_QUEUE = "dashboard.generation.queue";
    public static final String REPORT_GENERATION_QUEUE = "report.generation.queue";

    /**
     * Отправляет сообщение об уведомлении в соответствующую очередь
     * 
     * @param notification объект уведомления для отправки
     */
    public void sendNotification(NotificationMessage notification) {
        log.info("Sending notification message to queue: {}", notification);
        try {
            jmsTemplate.convertAndSend(NOTIFICATION_QUEUE, notification);
            log.info("Notification message sent successfully");
        } catch (Exception e) {
            log.error("Error sending notification message to queue", e);
            throw new RuntimeException("Failed to send notification message", e);
        }
    }

    /**
     * Отправляет запрос на генерацию дашборда в соответствующую очередь
     * 
     * @param request запрос на генерацию дашборда
     */
    public void sendDashboardGenerationRequest(DashboardGenerationRequest request) {
        log.info("Sending dashboard generation request to queue: {}", request);
        try {
            jmsTemplate.convertAndSend(DASHBOARD_GENERATION_QUEUE, request);
            log.info("Dashboard generation request sent successfully");
        } catch (Exception e) {
            log.error("Error sending dashboard generation request to queue", e);
            throw new RuntimeException("Failed to send dashboard generation request", e);
        }
    }

    /**
     * Отправляет произвольное сообщение в указанную очередь
     * 
     * @param destination имя очереди
     * @param message сообщение для отправки
     */
    public void sendMessage(String destination, Object message) {
        log.info("Sending message to queue {}: {}", destination, message);
        try {
            jmsTemplate.convertAndSend(destination, message);
            log.info("Message sent successfully to {}", destination);
        } catch (Exception e) {
            log.error("Error sending message to queue " + destination, e);
            throw new RuntimeException("Failed to send message to " + destination, e);
        }
    }
}
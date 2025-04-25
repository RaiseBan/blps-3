package com.example.blps.service.notification;

import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.Notification;
import com.example.blps.repository.notification.NotificationRepository;
import com.example.blps.service.integration.Bitrix24Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListenerService {

    private final NotificationRepository notificationRepository;
    private final Bitrix24Service bitrix24Service;

    /**
     * Обрабатывает входящие уведомления из очереди
     * 
     * @param message сообщение с уведомлением
     */
    @JmsListener(destination = MessageSenderService.NOTIFICATION_QUEUE)
    @Transactional
    public void processNotification(NotificationMessage message) {
        log.info("Received notification message: {}", message);
        
        try {
            // Сохраняем уведомление в базу данных
            Notification notification = createNotificationFromMessage(message);
            notificationRepository.save(notification);
            log.info("Notification saved to database with ID: {}", notification.getId());
            
            // Если получатель - администратор или аналитик, также отправляем уведомление в Bitrix24
            if (isHighPriorityRecipient(message.getRecipient())) {
                sendToBitrix24(message);
            }
            
            // Отмечаем уведомление как отправленное
            notification.setIsSent(true);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            log.error("Error processing notification", e);
            // Здесь можно реализовать логику повторной отправки или записи в журнал ошибок
        }
    }

    /**
     * Преобразует сообщение в объект уведомления
     */
    private Notification createNotificationFromMessage(NotificationMessage message) {
        Notification notification = new Notification();
        notification.setTitle(message.getTitle());
        notification.setMessage(message.getMessage());
        notification.setType(message.getType());
        notification.setRecipient(message.getRecipient());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setIsSent(false);
        return notification;
    }

    /**
     * Определяет, является ли получатель высокоприоритетным (администратор или аналитик)
     */
    private boolean isHighPriorityRecipient(String recipient) {
        return recipient.contains("ADMIN") || recipient.contains("ANALYST");
    }

    /**
     * Отправляет уведомление в Bitrix24 через JCA коннектор
     */
    private void sendToBitrix24(NotificationMessage message) {
        try {
            // Формируем задачу в Bitrix24
            String title = "Уведомление: " + message.getTitle();
            String description = "Тип: " + message.getType() + "\n" +
                               "Содержание: " + message.getMessage() + "\n" +
                               "Время: " + LocalDateTime.now();
            
            // Отправляем задачу через коннектор
            bitrix24Service.createTask(title, description, "1");
            log.info("Notification sent to Bitrix24: {}", title);
        } catch (Exception e) {
            log.error("Error sending notification to Bitrix24", e);
        }
    }
}
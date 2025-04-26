package com.example.blps.service.notification;

import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.service.integration.Bitrix24Service;
import jakarta.resource.ResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Сервис для прослушивания и обработки уведомлений из очереди.
 * Отправляет уведомления в Bitrix24 без сохранения в локальную БД.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListenerService {

    private final Bitrix24Service bitrix24Service;

    /**
     * Обрабатывает входящие уведомления из очереди и отправляет их в Bitrix24
     *
     * @param message сообщение с уведомлением
     */
    @JmsListener(destination = MessageSenderService.NOTIFICATION_QUEUE)
    public void processNotification(NotificationMessage message) {
        log.info("Received notification message: {}", message);

        try {
            // Отправляем уведомление в Bitrix24
            sendToBitrix24(message);
            log.info("Notification sent to Bitrix24 successfully");
        } catch (Exception e) {
            log.error("Error processing notification", e);
            // В случае ошибки можно реализовать механизм повторной отправки
            // или записать в журнал для дальнейшего анализа
        }
    }

    /**
     * Отправляет уведомление в Bitrix24
     */
    private void sendToBitrix24(NotificationMessage message) {
        try {
            // Формируем задачу в Bitrix24
            String title = "Уведомление: " + message.getTitle();
            String description = "Тип: " + message.getType() + "\n" +
                    "Содержание: " + message.getMessage() + "\n" +
                    "Время: " + LocalDateTime.now();

            if (message.getRelatedEntityId() != null) {
                description += "\nСвязанная сущность ID: " + message.getRelatedEntityId();
            }

            if (message.getAdditionalData() != null && !message.getAdditionalData().isEmpty()) {
                description += "\nДополнительные данные: " + message.getAdditionalData();
            }

            // Отправляем задачу через коннектор
            bitrix24Service.createTask(title, description, "1");
            log.info("Notification sent to Bitrix24: {}", title);
        } catch (ResourceException e) {
            log.error("Error sending notification to Bitrix24", e);
            throw new RuntimeException("Failed to send notification to Bitrix24", e);
        }
    }
}
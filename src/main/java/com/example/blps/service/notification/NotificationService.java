package com.example.blps.service.notification;

import com.example.blps.dto.notification.NotificationMessage;
import com.example.blps.model.notification.Notification;
import com.example.blps.model.notification.NotificationType;
import com.example.blps.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MessageSenderService messageSenderService;

    /**
     * Получает список всех уведомлений для указанного получателя
     * 
     * @param recipient получатель уведомлений
     * @return список уведомлений
     */
    public List<Notification> getNotificationsForUser(String recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    /**
     * Получает список непрочитанных уведомлений для указанного получателя
     * 
     * @param recipient получатель уведомлений
     * @return список непрочитанных уведомлений
     */
    public List<Notification> getUnreadNotificationsForUser(String recipient) {
        return notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(recipient);
    }

    /**
     * Отмечает уведомление как прочитанное
     * 
     * @param notificationId ID уведомления
     * @param recipient получатель уведомления
     * @return true, если уведомление успешно отмечено как прочитанное
     */
    @Transactional
    public boolean markAsRead(Long notificationId, String recipient) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Проверяем, что уведомление принадлежит указанному получателю
        if (!notification.getRecipient().equals(recipient)) {
            log.warn("Attempt to mark notification as read by wrong recipient: {}", recipient);
            return false;
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return true;
    }

    /**
     * Создает и отправляет новое уведомление
     * 
     * @param title заголовок уведомления
     * @param message текст уведомления
     * @param type тип уведомления
     * @param recipient получатель уведомления
     * @param relatedEntityId ID связанной сущности (опционально)
     * @return созданное уведомление
     */
    public Notification createAndSendNotification(String title, String message, NotificationType type, 
                                                 String recipient, Long relatedEntityId) {
        // Создаем объект сообщения для отправки в очередь
        NotificationMessage notificationMessage = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(type)
                .recipient(recipient)
                .relatedEntityId(relatedEntityId)
                .build();
        
        // Отправляем сообщение в очередь для асинхронной обработки
        messageSenderService.sendNotification(notificationMessage);
        
        // Для немедленного отображения в UI можно также сразу сохранить уведомление в базу
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRecipient(recipient);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setIsSent(false);
        
        return notificationRepository.save(notification);
    }

    /**
     * Получает список недавних уведомлений для указанного получателя
     * 
     * @param recipient получатель уведомлений
     * @param days количество дней для выборки
     * @return список недавних уведомлений
     */
    public List<Notification> getRecentNotifications(String recipient, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return notificationRepository.findRecentNotifications(recipient, since);
    }

    /**
     * Удаляет все уведомления для указанного получателя
     * 
     * @param recipient получатель уведомлений
     * @return количество удаленных уведомлений
     */
    @Transactional
    public int deleteAllNotificationsForUser(String recipient) {
        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
        int count = notifications.size();
        notificationRepository.deleteAll(notifications);
        return count;
    }
}
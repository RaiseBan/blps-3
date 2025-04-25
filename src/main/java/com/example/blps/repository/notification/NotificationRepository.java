package com.example.blps.repository.notification;

import com.example.blps.model.notification.Notification;
import com.example.blps.model.notification.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(String recipient);
    
    List<Notification> findByRecipientAndIsReadFalseOrderByCreatedAtDesc(String recipient);
    
    List<Notification> findByTypeAndCreatedAtAfter(NotificationType type, LocalDateTime since);
    
    List<Notification> findByIsSentFalseOrderByCreatedAtAsc();
    
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("recipient") String recipient, @Param("since") LocalDateTime since);
}
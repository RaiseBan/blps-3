package com.example.blps.dto.notification;

import com.example.blps.model.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String title;
    private String message;
    private String receiver;
    private NotificationType type;
    private String recipient;
    private Long relatedEntityId;
    private String additionalData;
}
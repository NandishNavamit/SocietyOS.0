package com.database.societyproject.notification.dto;

import com.database.societyproject.notification.entity.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(Long id, Long societyId, Long recipientUserId,
                                   NotificationType type, String title, String message,
                                   Boolean read, LocalDateTime readAt, LocalDateTime createdAt) { }

package com.database.societyproject.notification.dto;

import com.database.societyproject.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationRequest(
        @NotNull Long societyId,
        @NotNull Long recipientUserId,
        @NotNull NotificationType type,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2000) String message) { }

package com.fairliving.backend.notification.dto;

import com.fairliving.backend.notification.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        NotificationType type,
        String message,
        Instant createdAt
) {
}

package com.fairliving.backend.notification.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record NotificationsReadRequest(
        @NotNull List<UUID> notificationIds
) {
}

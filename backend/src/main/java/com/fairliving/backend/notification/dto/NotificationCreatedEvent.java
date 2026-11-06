package com.fairliving.backend.notification.dto;

import java.util.Set;
import java.util.UUID;

public record NotificationCreatedEvent(
        Set<UUID> householdMemberIds
) {
}

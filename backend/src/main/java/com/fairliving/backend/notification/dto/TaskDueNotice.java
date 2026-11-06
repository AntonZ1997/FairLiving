package com.fairliving.backend.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record TaskDueNotice(
        UUID householdMemberId,
        String taskName,
        Instant dueDate
) {
}

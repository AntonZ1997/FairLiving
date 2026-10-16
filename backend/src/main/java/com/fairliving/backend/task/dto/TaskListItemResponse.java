package com.fairliving.backend.task.dto;

import java.time.Instant;
import java.util.UUID;

public record TaskListItemResponse(
        UUID taskId,
        String name,
        String difficultyName,
        int difficultyWeight,
        int intervalDays,
        Instant dueDate,
        String assignedMemberName
) {
}

package com.fairliving.backend.task.dto;

import java.time.Instant;
import java.util.UUID;

public record AssignedTaskResponse(
        UUID assignedTaskId,
        String name,
        String description,
        String difficultyName,
        int xpReward,
        Instant dueDate,
        String assignedMemberName
) {
}

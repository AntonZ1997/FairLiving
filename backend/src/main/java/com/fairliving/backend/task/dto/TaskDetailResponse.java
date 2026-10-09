package com.fairliving.backend.task.dto;

import java.time.Instant;
import java.util.UUID;

public record TaskDetailResponse(
        UUID taskId,
        String name,
        String description,
        String difficultyName,
        int xpReward,
        int intervalDays,
        boolean active,
        UUID assignedTaskId,
        String assignedMemberName,
        Instant dueDate,
        boolean assignedToCurrentUser,
        int completedByCurrentUser
) {
}

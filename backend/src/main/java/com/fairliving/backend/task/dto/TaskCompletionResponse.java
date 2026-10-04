package com.fairliving.backend.task.dto;

public record TaskCompletionResponse(
        int earnedExperiencePoints,
        int totalExperiencePoints,
        int streakCount,
        int levelNumber,
        String levelTitle,
        boolean leveledUp,
        boolean completedOnTime
) {
}

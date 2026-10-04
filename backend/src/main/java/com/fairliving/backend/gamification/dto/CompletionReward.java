package com.fairliving.backend.gamification.dto;

import de.fairliving.backend.jooq.tables.records.LevelRecord;

public record CompletionReward(
        int earnedExperiencePoints,
        int totalExperiencePoints,
        int streakCount,
        LevelRecord level,
        boolean leveledUp
) {
}

package com.fairliving.backend.statistics.dto;

import java.time.Instant;

public record CumulatedExperiencePoints(
        Instant completedAt,
        int cumulatedExperiencePoints
) {
}

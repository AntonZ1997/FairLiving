package com.fairliving.backend.task.dto;

import java.util.UUID;

public record DifficultyResponse(
        UUID id,
        String name,
        int baseXp,
        int weight
) {
}

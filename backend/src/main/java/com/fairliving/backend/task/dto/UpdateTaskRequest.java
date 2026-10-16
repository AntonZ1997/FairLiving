package com.fairliving.backend.task.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public record UpdateTaskRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull UUID difficultyId,
        @Min(0) int intervalDays,
        @Future Instant dueDate
) {
    public UpdateTaskRequest {
        name = name == null ? null : name.strip();
        description = description == null ? null : description.strip();
    }
}

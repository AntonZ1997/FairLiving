package com.fairliving.backend.task.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @NotNull UUID difficultyId,
        @NotNull @Future Instant dueDate,
        @Min(0) int intervalDays
) {
    public CreateTaskRequest {
        name = name == null ? null : name.strip();
        description = description == null ? null : description.strip();
    }
}

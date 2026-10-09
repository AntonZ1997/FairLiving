package com.fairliving.backend.task.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ActivateTaskRequest(
        @NotNull @Future Instant dueDate) {
}

package com.fairliving.backend.statistics.dto;

public record CompletionShareResponse(
        int onTime,
        int late,
        int open
) {
}

package com.fairliving.backend.statistics.dto;

public record HouseholdMemberCompletionCount(
        String householdMemberName,
        int completedCount
) {
}

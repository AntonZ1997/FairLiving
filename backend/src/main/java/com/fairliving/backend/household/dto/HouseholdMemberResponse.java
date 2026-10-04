package com.fairliving.backend.household.dto;

import com.fairliving.backend.household.HouseholdRole;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record HouseholdMemberResponse(
        UUID memberId,
        String userName,
        HouseholdRole role,
        int experiencePoints,
        int streakCount,
        int levelNumber,
        String levelTitle,
        int currentLevelRequiredXp,
        Integer nextLevelRequiredXp,
        @JsonProperty("isCurrentUser") boolean isCurrentUser,
        Instant joined
) {
}

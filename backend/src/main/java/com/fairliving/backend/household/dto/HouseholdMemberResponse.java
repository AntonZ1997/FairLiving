package com.fairliving.backend.household.dto;

import com.fairliving.backend.household.HouseholdRole;

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
        Instant joined
) {
}

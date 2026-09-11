package com.fairliving.backend.household.dto;

import com.fairliving.backend.household.HouseholdRole;

import java.time.Instant;
import java.util.UUID;

public record HouseholdSummaryResponse(
        UUID householdId,
        String householdName,
        HouseholdRole role,
        Instant joined
) {
}

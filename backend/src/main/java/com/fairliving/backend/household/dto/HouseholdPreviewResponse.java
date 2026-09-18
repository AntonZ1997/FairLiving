package com.fairliving.backend.household.dto;

import java.time.Instant;

public record HouseholdPreviewResponse(
        String name,
        int memberCount,
        Instant createdAt,
        boolean alreadyMember
) {
}

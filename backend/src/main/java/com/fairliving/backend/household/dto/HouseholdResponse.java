package com.fairliving.backend.household.dto;

import de.fairliving.backend.jooq.tables.records.HouseholdRecord;

import java.time.Instant;
import java.util.UUID;

public record HouseholdResponse(
        UUID id,
        String name,
        String invitationToken,
        Instant createdAt
) {
    public static HouseholdResponse from(HouseholdRecord record) {
        return new HouseholdResponse(
                record.getId(),
                record.getName(),
                record.getInvitationLink(),
                record.getCreatedAt()
        );
    }
}

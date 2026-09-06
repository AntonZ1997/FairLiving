package com.fairliving.backend.user;

import de.fairliving.backend.jooq.tables.records.UserRecord;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String userName,
        Instant createdAt
) {
    public static UserResponse from(UserRecord user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUserName(),
                user.getCreatedAt()
        );
    }
}

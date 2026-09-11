package com.fairliving.backend.exception;

import java.util.UUID;

public class UserIsNotHouseholdAdminException extends RuntimeException {
    public UserIsNotHouseholdAdminException(UUID householdId, UUID userId) {
        super("User " + userId + " is not an admin of household " + householdId);
    }
}

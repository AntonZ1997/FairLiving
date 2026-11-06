package com.fairliving.backend.exception;

import java.util.UUID;

public class UserIsAlreadyHouseholdMemberException extends RuntimeException {
    public UserIsAlreadyHouseholdMemberException(UUID householdId, UUID userId)
    {
        super("User " + userId + " is already a member of household " + householdId);
    }
}

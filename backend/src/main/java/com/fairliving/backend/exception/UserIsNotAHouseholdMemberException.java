package com.fairliving.backend.exception;

import java.util.UUID;

public class UserIsNotAHouseholdMemberException extends RuntimeException {
    public UserIsNotAHouseholdMemberException(UUID householdId, UUID userId) {
        super("User " + userId + " is not a member of household " +  householdId);
    }
}

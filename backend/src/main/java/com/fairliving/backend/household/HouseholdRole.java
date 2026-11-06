package com.fairliving.backend.household;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HouseholdRole {

    ADMIN("admin"),
    MEMBER("member");

    private final String role;

    HouseholdRole(String role) {
        this.role = role;
    }

    @JsonValue
    public String getRole() {
        return role;
    }

    @JsonCreator
    public static HouseholdRole fromString(String role) {
        for (HouseholdRole householdRole : HouseholdRole.values()) {
            if (householdRole.getRole().equals(role)) {
                return householdRole;
            }
        }
        throw new IllegalArgumentException("HouseholdRole not found: " + role);
    }
}

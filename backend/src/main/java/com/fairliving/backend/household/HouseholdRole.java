package com.fairliving.backend.household;

public enum HouseholdRole {

    ADMIN("admin"),
    MEMBER("member");

    private final String role;

    HouseholdRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static HouseholdRole fromString(String role) {
        for (HouseholdRole householdRole : HouseholdRole.values()) {
            if (householdRole.getRole().equals(role)) {
                return householdRole;
            }
        }
        throw new IllegalArgumentException("HouseholdRole not found: " + role);
    }
}

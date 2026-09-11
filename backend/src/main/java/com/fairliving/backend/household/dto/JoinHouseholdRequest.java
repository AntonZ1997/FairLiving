package com.fairliving.backend.household.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinHouseholdRequest(
        @NotBlank String invitationToken
) {
    public JoinHouseholdRequest {
        invitationToken = invitationToken == null ? null : invitationToken.strip();
    }
}

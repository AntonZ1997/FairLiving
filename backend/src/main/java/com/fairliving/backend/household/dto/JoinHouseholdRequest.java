package com.fairliving.backend.household.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record JoinHouseholdRequest(
        @NotNull UUID invitationId
) {}

package com.fairliving.backend.auth.dao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
    public LoginRequest {
        email = email == null ? null : email.strip().toLowerCase();
    }
}

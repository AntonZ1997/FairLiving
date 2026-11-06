package com.fairliving.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "Das Passwort muss mindestens 8 Zeichen lang sein, Groß- und Kleinbuchstaben, eine Zahl sowie ein Sonderzeichen enthalten."
        )
        String password,
        @NotBlank String userName
) {
        public RegisterRequest {
                email = email == null ? null : email.strip().toLowerCase();
                userName = userName == null ? null : userName.strip();
        }
}

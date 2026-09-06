package com.fairliving.backend.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String email) {
        super("E-Mail already exists: " + email);
    }
}

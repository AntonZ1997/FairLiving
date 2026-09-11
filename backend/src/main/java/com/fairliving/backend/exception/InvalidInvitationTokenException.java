package com.fairliving.backend.exception;

public class InvalidInvitationTokenException extends RuntimeException {
    public InvalidInvitationTokenException()
    {
        super("Invalid Invitation Token");
    }
}

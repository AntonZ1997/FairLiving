package com.fairliving.backend.exception;

public class InvalidInvitationIdException extends RuntimeException {
    public InvalidInvitationIdException()
    {
        super("Invalid Invitation Token");
    }
}

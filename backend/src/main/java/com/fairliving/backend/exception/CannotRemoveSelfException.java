package com.fairliving.backend.exception;

import java.util.UUID;

public class CannotRemoveSelfException extends RuntimeException {
    public CannotRemoveSelfException(UUID memberId) {
        super("A household member cannot remove themselves: " + memberId);
    }
}

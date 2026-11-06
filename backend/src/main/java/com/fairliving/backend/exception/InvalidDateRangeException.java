package com.fairliving.backend.exception;

import java.time.Instant;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(Instant from, Instant to) {
        super("Invalid date range from " + from.toString() + " to " + to.toString());
    }
}

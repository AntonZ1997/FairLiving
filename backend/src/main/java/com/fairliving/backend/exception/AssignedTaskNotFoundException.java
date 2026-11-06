package com.fairliving.backend.exception;

import java.util.UUID;

public class AssignedTaskNotFoundException extends RuntimeException {
    public AssignedTaskNotFoundException(UUID assignedTaskId) {
        super("Assigned task not found: "  + assignedTaskId);
    }
}

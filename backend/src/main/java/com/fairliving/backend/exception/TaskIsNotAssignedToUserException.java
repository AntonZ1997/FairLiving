package com.fairliving.backend.exception;

import java.util.UUID;

public class TaskIsNotAssignedToUserException extends RuntimeException {
    public TaskIsNotAssignedToUserException(UUID assignedTaskId, UUID userId) {
        super("Task " + assignedTaskId + " is not assigned to user " + userId);
    }
}

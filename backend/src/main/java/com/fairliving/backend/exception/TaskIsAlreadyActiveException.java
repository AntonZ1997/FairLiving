package com.fairliving.backend.exception;

import java.util.UUID;

public class TaskIsAlreadyActiveException extends RuntimeException {
    public TaskIsAlreadyActiveException(UUID taskId) {
        super("Task is already active: " + taskId);
    }
}

package com.fairliving.backend.exception;

import java.util.UUID;

public class TaskAlreadyCompletedException extends RuntimeException {
    public TaskAlreadyCompletedException(UUID assignedTaskId) {
        super("Assigned task is already completed: " + assignedTaskId);
    }
}

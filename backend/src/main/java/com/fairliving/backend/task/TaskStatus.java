package com.fairliving.backend.task;

public enum TaskStatus {

    OPEN("open"),
    COMPLETED("completed");

    private final String status;

    TaskStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public static TaskStatus fromString(String status) {
        for (TaskStatus taskStatus : TaskStatus.values()) {
            if (taskStatus.status.equals(status)) {
                return taskStatus;
            }
        }
        throw new IllegalArgumentException("TaskStatus not found: "  + status);

    }
}

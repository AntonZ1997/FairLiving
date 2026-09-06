package com.fairliving.backend.notification;

public enum NotificationType {

    INFO("info"),
    WARNING("warning"),
    ALERT("alert");

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static NotificationType fromString(String type) {
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationType.type.equals(type)) {
                return notificationType;
            }
        }
        throw new IllegalArgumentException("NotificationType not found: " + type);
    }

}

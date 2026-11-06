package com.fairliving.backend.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {

    INFO("info"),
    WARNING("warning"),
    ALERT("alert");

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }

    @JsonCreator
    public static NotificationType fromString(String type) {
        for (NotificationType notificationType : NotificationType.values()) {
            if (notificationType.type.equals(type)) {
                return notificationType;
            }
        }
        throw new IllegalArgumentException("NotificationType not found: " + type);
    }

}

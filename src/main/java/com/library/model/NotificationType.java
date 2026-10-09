package com.library.model;

public enum NotificationType {
    DUE_SOON,
    OVERDUE,
    RESERVATION_READY,
    NEW_ARRIVAL,
    BORROW_CONFIRMATION,
    RETURN_CONFIRMATION,
    FINE_CREATED,
    GENERAL;

    public static NotificationType fromString(String typeStr) {
        if (typeStr == null) return GENERAL;
        try {
            return NotificationType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL;
        }
    }
}

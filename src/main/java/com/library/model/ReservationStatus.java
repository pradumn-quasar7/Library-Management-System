package com.library.model;

public enum ReservationStatus {
    WAITING,
    READY,
    FULFILLED,
    CANCELLED,
    EXPIRED;

    public static ReservationStatus fromString(String statusStr) {
        if (statusStr == null) return null;
        try {
            return ReservationStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

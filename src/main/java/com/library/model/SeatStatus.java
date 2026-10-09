package com.library.model;

public enum SeatStatus {
    AVAILABLE,
    MAINTENANCE;

    public static SeatStatus fromString(String statusStr) {
        if (statusStr == null) return AVAILABLE;
        try {
            return SeatStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AVAILABLE;
        }
    }
}

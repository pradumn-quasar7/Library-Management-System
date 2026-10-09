package com.library.model;

public enum BookingStatus {
    BOOKED,
    CANCELLED,
    COMPLETED;

    public static BookingStatus fromString(String statusStr) {
        if (statusStr == null) return BOOKED;
        try {
            return BookingStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return BOOKED;
        }
    }
}

package com.library.model;

public enum FineStatus {
    UNPAID,
    PAID,
    WAIVED;

    public static FineStatus fromString(String statusStr) {
        if (statusStr == null) return null;
        try {
            return FineStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

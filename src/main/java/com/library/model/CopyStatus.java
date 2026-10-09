package com.library.model;

public enum CopyStatus {
    AVAILABLE,
    BORROWED,
    RESERVED,
    LOST,
    DAMAGED,
    MAINTENANCE;

    public static CopyStatus fromString(String statusStr) {
        if (statusStr == null) return null;
        try {
            return CopyStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

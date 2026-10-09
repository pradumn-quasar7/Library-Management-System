package com.library.model;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED;

    public static UserStatus fromString(String statusStr) {
        if (statusStr == null) return null;
        try {
            return UserStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

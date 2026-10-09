package com.library.model;

public enum LoanStatus {
    ACTIVE,
    RETURNED,
    OVERDUE,
    LOST;

    public static LoanStatus fromString(String statusStr) {
        if (statusStr == null) return null;
        try {
            return LoanStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

package com.library.validation;

public class PasswordValidator {
    public static boolean isValid(String password) {
        if (password == null) return false;
        String trimmed = password.trim();
        return trimmed.length() >= 6;
    }
}

package com.library.validation;

import com.library.model.Member;

public class MemberValidator {
    public static ValidationResult validate(Member member, String email, String password, boolean isNewRegistration) {
        ValidationResult result = new ValidationResult();
        if (member == null) {
            result.addError("Member cannot be null");
            return result;
        }

        if (member.getFullName() == null || member.getFullName().trim().isEmpty()) {
            result.addError("Full name is required");
        } else if (member.getFullName().trim().length() > 150) {
            result.addError("Full name cannot exceed 150 characters");
        }

        if (email == null || !EmailValidator.isValid(email)) {
            result.addError("A valid email address is required");
        }

        if (isNewRegistration) {
            if (password == null || !PasswordValidator.isValid(password)) {
                result.addError("Password must be at least 6 characters long");
            }
        } else if (password != null && !password.trim().isEmpty() && !PasswordValidator.isValid(password)) {
            result.addError("New password must be at least 6 characters long");
        }

        if (member.getPhone() != null && !member.getPhone().trim().isEmpty()) {
            if (member.getPhone().trim().length() > 30) {
                result.addError("Phone number cannot exceed 30 characters");
            }
        }

        return result;
    }
}

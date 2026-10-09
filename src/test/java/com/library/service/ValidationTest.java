package com.library.service;

import com.library.validation.EmailValidator;
import com.library.validation.ISBNValidator;
import com.library.validation.PasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationTest {

    @Test
    @DisplayName("Validate ISBN-10 checksums")
    public void testIsbn10Validation() {
        assertTrue(ISBNValidator.isValid("0-306-40615-2"));
        assertTrue(ISBNValidator.isValid("0306406152"));
        assertTrue(ISBNValidator.isValid("0-8044-2957-X"));
        assertFalse(ISBNValidator.isValid("0-306-40615-3")); // Wrong check digit
        assertFalse(ISBNValidator.isValid("invalid-isbn"));
    }

    @Test
    @DisplayName("Validate ISBN-13 checksums")
    public void testIsbn13Validation() {
        assertTrue(ISBNValidator.isValid("978-0134685991")); // Effective Java
        assertTrue(ISBNValidator.isValid("9780134685991"));
        assertTrue(ISBNValidator.isValid("978-0132350884")); // Clean Code
        assertFalse(ISBNValidator.isValid("978-0134685992")); // Wrong check digit
    }

    @Test
    @DisplayName("Validate Email formats")
    public void testEmailValidation() {
        assertTrue(EmailValidator.isValid("admin@library.local"));
        assertTrue(EmailValidator.isValid("john.smith@example.com"));
        assertFalse(EmailValidator.isValid("plainaddress"));
        assertFalse(EmailValidator.isValid("@missingusername.com"));
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    @DisplayName("Validate Password length and complexity")
    public void testPasswordValidation() {
        assertTrue(PasswordValidator.isValid("Admin@123"));
        assertTrue(PasswordValidator.isValid("123456"));
        assertFalse(PasswordValidator.isValid("12345")); // Under 6 characters
        assertFalse(PasswordValidator.isValid(null));
    }
}

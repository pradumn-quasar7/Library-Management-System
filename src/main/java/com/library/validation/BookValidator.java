package com.library.validation;

import com.library.model.Book;

import java.time.Year;

public class BookValidator {
    public static ValidationResult validate(Book book) {
        ValidationResult result = new ValidationResult();
        if (book == null) {
            result.addError("Book cannot be null");
            return result;
        }

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            result.addError("Book title is required");
        } else if (book.getTitle().trim().length() > 255) {
            result.addError("Book title cannot exceed 255 characters");
        }

        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            result.addError("ISBN is required");
        } else if (!ISBNValidator.isValid(book.getIsbn())) {
            result.addError("Invalid ISBN format (must be valid ISBN-10 or ISBN-13)");
        }

        if (book.getPublicationYear() != null) {
            int currentYear = Year.now().getValue();
            if (book.getPublicationYear() < 1400 || book.getPublicationYear() > currentYear + 1) {
                result.addError("Publication year must be between 1400 and " + (currentYear + 1));
            }
        }

        if (book.getGenreId() == null || book.getGenreId() <= 0) {
            result.addError("Genre selection is required");
        }

        return result;
    }
}

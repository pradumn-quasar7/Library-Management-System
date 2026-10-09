package com.library.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends LibraryException {
    private final List<String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(message);
    }

    public ValidationException(List<String> errors) {
        super(errors != null && !errors.isEmpty() ? String.join("; ", errors) : "Validation failed");
        this.errors = (errors != null) ? errors : new ArrayList<>();
    }

    public List<String> getErrors() {
        return errors;
    }
}

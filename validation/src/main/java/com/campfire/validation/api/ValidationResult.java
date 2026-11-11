package com.campfire.validation.api;

import java.util.List;

/** Outcome of one rule. An empty error list means the input passed. */
public record ValidationResult(List<String> errors) {

    private static final ValidationResult OK = new ValidationResult(List.of());

    public ValidationResult {
        errors = List.copyOf(errors);
    }

    public static ValidationResult ok() {
        return OK;
    }

    public static ValidationResult fail(String... errors) {
        return new ValidationResult(List.of(errors));
    }

    public static ValidationResult of(List<String> errors) {
        return errors.isEmpty() ? OK : new ValidationResult(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}

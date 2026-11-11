package com.campfire.ledger.validation;

import com.campfire.validation.api.ValidationRule;
import java.util.ArrayList;
import java.util.List;

/** Holds every loaded rule and runs the ones whose target matches the input's type. Built by ValidationConfig. */
public class ValidationRegistry {

    private final List<ValidationRule<?>> rules;

    public ValidationRegistry(List<ValidationRule<?>> rules) {
        this.rules = List.copyOf(rules);
    }

    public List<ValidationRule<?>> rules() {
        return rules;
    }

    /** All error messages from all matching rules. Empty means valid. */
    @SuppressWarnings("unchecked")
    public <T> List<String> validate(T input) {
        List<String> errors = new ArrayList<>();
        for (ValidationRule<?> rule : rules) {
            if (rule.target().isInstance(input)) {
                errors.addAll(((ValidationRule<T>) rule).validate(input).errors());
            }
        }
        return errors;
    }
}

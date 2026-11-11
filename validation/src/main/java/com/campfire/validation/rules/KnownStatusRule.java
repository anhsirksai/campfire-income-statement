package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Only posted entries count, so an unknown status (e.g. a typo of "posted") would silently hide an entry. */
public enum KnownStatusRule implements ValidationRule<LedgerSnapshot> {
    INSTANCE;

    private static final Set<String> KNOWN = Set.of("posted", "draft", "void");

    @Override
    public Class<LedgerSnapshot> target() {
        return LedgerSnapshot.class;
    }

    @Override
    public ValidationResult validate(LedgerSnapshot input) {
        List<String> errors = new ArrayList<>();
        for (LedgerSnapshot.Entry entry : input.entries()) {
            // Null check first: Set.of(...).contains(null) throws instead of returning false.
            if (entry.status() == null || !KNOWN.contains(entry.status())) {
                errors.add(entry.id() + " has unknown status '" + entry.status() + "'");
            }
        }
        return ValidationResult.of(errors);
    }
}

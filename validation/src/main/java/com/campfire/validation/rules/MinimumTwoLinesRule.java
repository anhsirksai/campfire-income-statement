package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.util.ArrayList;
import java.util.List;

/** Double entry needs a debit side and a credit side. An empty entry would otherwise pass the balance check (0 = 0). */
public enum MinimumTwoLinesRule implements ValidationRule<LedgerSnapshot> {
    INSTANCE;

    @Override
    public Class<LedgerSnapshot> target() {
        return LedgerSnapshot.class;
    }

    @Override
    public ValidationResult validate(LedgerSnapshot input) {
        List<String> errors = new ArrayList<>();
        for (LedgerSnapshot.Entry entry : input.entries()) {
            int count = entry.lines().size();
            if (count < 2) {
                errors.add(entry.id() + " has " + count + (count == 1 ? " line" : " lines")
                        + "; an entry needs at least 2");
            }
        }
        return ValidationResult.of(errors);
    }
}

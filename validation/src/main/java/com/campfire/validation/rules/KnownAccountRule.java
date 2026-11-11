package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.util.ArrayList;
import java.util.List;

/** A line posted to an account missing from the chart of accounts would silently drop out of every report. */
public enum KnownAccountRule implements ValidationRule<LedgerSnapshot> {
    INSTANCE;

    @Override
    public Class<LedgerSnapshot> target() {
        return LedgerSnapshot.class;
    }

    @Override
    public ValidationResult validate(LedgerSnapshot input) {
        List<String> errors = new ArrayList<>();
        for (LedgerSnapshot.Entry entry : input.entries()) {
            for (int i = 0; i < entry.lines().size(); i++) {
                String account = entry.lines().get(i).account();
                // Null check first: an immutable Set's contains(null) throws instead of returning false.
                if (account == null || !input.accountNumbers().contains(account)) {
                    errors.add(entry.id() + " line " + (i + 1) + " uses unknown account " + account);
                }
            }
        }
        return ValidationResult.of(errors);
    }
}

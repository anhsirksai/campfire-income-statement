package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.util.ArrayList;
import java.util.List;

/**
 * Fields the database requires must be present in ledger.json. Without this rule the other rules treat a
 * missing amount as 0, so the ledger passes validation and then fails on insert with a stack trace.
 * Missing status, account and type/subtype are already reported by their own rules; missing lines by
 * MinimumTwoLinesRule (the loader passes them as an empty list).
 */
public enum RequiredFieldsRule implements ValidationRule<LedgerSnapshot> {
    INSTANCE;

    @Override
    public Class<LedgerSnapshot> target() {
        return LedgerSnapshot.class;
    }

    @Override
    public ValidationResult validate(LedgerSnapshot input) {
        List<String> errors = new ArrayList<>();
        for (int a = 0; a < input.accounts().size(); a++) {
            if (input.accounts().get(a).number() == null) {
                errors.add("account " + (a + 1) + " is missing a number");
            }
        }
        for (int e = 0; e < input.entries().size(); e++) {
            LedgerSnapshot.Entry entry = input.entries().get(e);
            if (entry.id() == null) {
                errors.add("journal entry " + (e + 1) + " is missing an id");
            }
            if (entry.date() == null) {
                errors.add(entry.id() + " is missing a date");
            }
            for (int i = 0; i < entry.lines().size(); i++) {
                LedgerSnapshot.Line line = entry.lines().get(i);
                if (line.debit() == null) {
                    errors.add(entry.id() + " line " + (i + 1) + " is missing a debit amount");
                }
                if (line.credit() == null) {
                    errors.add(entry.id() + " line " + (i + 1) + " is missing a credit amount");
                }
            }
        }
        return ValidationResult.of(errors);
    }
}

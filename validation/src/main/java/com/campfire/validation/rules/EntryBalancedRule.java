package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Double entry: every journal entry's debits must equal its credits, or the books are wrong. */
public enum EntryBalancedRule implements ValidationRule<LedgerSnapshot> {
    INSTANCE;

    @Override
    public Class<LedgerSnapshot> target() {
        return LedgerSnapshot.class;
    }

    @Override
    public ValidationResult validate(LedgerSnapshot input) {
        List<String> errors = new ArrayList<>();
        for (LedgerSnapshot.Entry entry : input.entries()) {
            BigDecimal debits = BigDecimal.ZERO;
            BigDecimal credits = BigDecimal.ZERO;
            for (LedgerSnapshot.Line line : entry.lines()) {
                debits = debits.add(orZero(line.debit()));
                credits = credits.add(orZero(line.credit()));
            }
            // compareTo, not equals: 100.0 and 100.00 are the same amount with different scales.
            if (debits.compareTo(credits) != 0) {
                errors.add(entry.id() + " is not balanced: debits " + debits.toPlainString()
                        + ", credits " + credits.toPlainString());
            }
        }
        return ValidationResult.of(errors);
    }

    private static BigDecimal orZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}

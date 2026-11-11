package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** A journal line is either a debit or a credit. Both set (or neither) means the line is ambiguous. */
public enum LineOneSidedRule implements ValidationRule<LedgerSnapshot> {
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
                LedgerSnapshot.Line line = entry.lines().get(i);
                BigDecimal debit = orZero(line.debit());
                BigDecimal credit = orZero(line.credit());
                boolean hasDebit = debit.signum() != 0;
                boolean hasCredit = credit.signum() != 0;
                if (hasDebit == hasCredit) {
                    errors.add(entry.id() + " line " + (i + 1) + " must have exactly one of debit or credit: debit "
                            + debit.toPlainString() + ", credit " + credit.toPlainString());
                }
            }
        }
        return ValidationResult.of(errors);
    }

    private static BigDecimal orZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}

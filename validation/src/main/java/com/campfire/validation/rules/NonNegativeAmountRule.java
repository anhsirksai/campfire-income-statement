package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Direction comes from debit vs credit, never from a minus sign. A negative amount would flip the sign silently. */
public enum NonNegativeAmountRule implements ValidationRule<LedgerSnapshot> {
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
                if (debit.signum() < 0 || credit.signum() < 0) {
                    errors.add(entry.id() + " line " + (i + 1) + " has a negative amount: debit "
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

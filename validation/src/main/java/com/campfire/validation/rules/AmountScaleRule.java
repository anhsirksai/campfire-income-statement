package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Amounts are whole cents. The DECIMAL(19,2) columns would silently round 0.005, so this rule is the only
 * guard. Trailing zeros are fine: 100, 100.0 and 100.00 are the same amount.
 */
public enum AmountScaleRule implements ValidationRule<LedgerSnapshot> {
    INSTANCE;

    private static final int MAX_DECIMALS = 2;

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
                check(errors, entry.id(), i + 1, "debit", line.debit());
                check(errors, entry.id(), i + 1, "credit", line.credit());
            }
        }
        return ValidationResult.of(errors);
    }

    private static void check(List<String> errors, String entryId, int lineNo, String side, BigDecimal amount) {
        if (amount != null && amount.stripTrailingZeros().scale() > MAX_DECIMALS) {
            errors.add(entryId + " line " + lineNo + " has more than " + MAX_DECIMALS + " decimal places: "
                    + side + " " + amount.toPlainString());
        }
    }
}

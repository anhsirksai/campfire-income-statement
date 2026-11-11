package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Type and subtype must agree. The statement takes the section from the subtype but the sign from the
 * type, so e.g. an expense-typed account with subtype operating_revenue would land in Revenue with the
 * wrong sign. Same pairs as the chk_account_classification constraint in schema.sql.
 */
public enum AccountClassificationRule implements ValidationRule<LedgerSnapshot> {
    INSTANCE;

    private static final Map<String, Set<String>> ALLOWED_SUBTYPES = Map.of(
            "revenue", Set.of("operating_revenue", "contra_revenue", "other_income"),
            "expense", Set.of("cogs", "operating_expense"),
            "asset", Set.of("balance_sheet"),
            "liability", Set.of("balance_sheet"),
            "equity", Set.of("balance_sheet"));

    @Override
    public Class<LedgerSnapshot> target() {
        return LedgerSnapshot.class;
    }

    @Override
    public ValidationResult validate(LedgerSnapshot input) {
        List<String> errors = new ArrayList<>();
        for (LedgerSnapshot.Account account : input.accounts()) {
            Set<String> allowed = account.type() == null ? null : ALLOWED_SUBTYPES.get(account.type());
            if (allowed == null) {
                errors.add("account " + account.number() + " has unknown type " + account.type());
            } else if (account.subtype() == null || !allowed.contains(account.subtype())) {
                errors.add("account " + account.number() + " has type " + account.type()
                        + " but subtype " + account.subtype());
            }
        }
        return ValidationResult.of(errors);
    }
}

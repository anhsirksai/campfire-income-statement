package com.campfire.validation.rules;

import com.campfire.validation.api.ValidationRule;
import java.util.List;

/** Every rule the app runs. Add each new rule's INSTANCE here (ValidationRulesTest fails if you forget). */
public final class ValidationRules {

    private ValidationRules() {
    }

    public static List<ValidationRule<?>> all() {
        return List.of(
                // Request: ReportPeriodInput
                DateRangePresentRule.INSTANCE,
                StartNotAfterEndRule.INSTANCE,
                // Ledger data at startup: LedgerSnapshot
                EntryBalancedRule.INSTANCE,
                LineOneSidedRule.INSTANCE,
                NonNegativeAmountRule.INSTANCE,
                KnownAccountRule.INSTANCE,
                KnownStatusRule.INSTANCE,
                MinimumTwoLinesRule.INSTANCE);
    }
}

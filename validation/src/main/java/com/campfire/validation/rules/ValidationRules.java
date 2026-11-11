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
                // Ledger data at startup: LedgerSnapshot. Missing and duplicate fields first, so they lead the error list.
                RequiredFieldsRule.INSTANCE,
                UniqueIdsRule.INSTANCE,
                EntryBalancedRule.INSTANCE,
                LineOneSidedRule.INSTANCE,
                NonNegativeAmountRule.INSTANCE,
                AmountScaleRule.INSTANCE,
                KnownAccountRule.INSTANCE,
                AccountClassificationRule.INSTANCE,
                KnownStatusRule.INSTANCE,
                MinimumTwoLinesRule.INSTANCE);
    }
}

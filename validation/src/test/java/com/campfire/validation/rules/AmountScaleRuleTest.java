package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AmountScaleRuleTest {

    private final AmountScaleRule rule = AmountScaleRule.INSTANCE;

    @Test
    void trailingZerosBeyondCentsAreFine() {
        assertThat(rule.validate(ledger(entry("JE-1", debit("1000", "100.000"), credit("4000", "100.0")))).isValid())
                .isTrue();
    }

    @Test
    void subCentAmountFails() {
        var result = rule.validate(ledger(entry("JE-1", debit("1000", "0.005"), credit("4000", "0.005"))));
        assertThat(result.errors()).containsExactly(
                "JE-1 line 1 has more than 2 decimal places: debit 0.005",
                "JE-1 line 2 has more than 2 decimal places: credit 0.005");
    }
}

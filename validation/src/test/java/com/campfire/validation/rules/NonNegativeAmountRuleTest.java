package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static com.campfire.validation.rules.Ledgers.line;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NonNegativeAmountRuleTest {

    private final NonNegativeAmountRule rule = NonNegativeAmountRule.INSTANCE;

    @Test
    void positiveAmountsAreValid() {
        var ledger = ledger(entry("JE-001", debit("1000", "10.00"), credit("4000", "10.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void negativeCreditFails() {
        var ledger = ledger(entry("JE-005", debit("1000", "5.00"), credit("4000", "-5.00")));
        assertThat(rule.validate(ledger).errors())
                .containsExactly("JE-005 line 2 has a negative amount: debit 0.00, credit -5.00");
    }

    @Test
    void negativeDebitFails() {
        var ledger = ledger(entry("JE-005", debit("1000", "-5.00"), credit("4000", "5.00")));
        assertThat(rule.validate(ledger).errors())
                .containsExactly("JE-005 line 1 has a negative amount: debit -5.00, credit 0.00");
    }

    @Test
    void reportsEveryNegativeLine() {
        var ledger = ledger(
                entry("JE-001", line("1000", "-1.00", "0.00"), credit("4000", "1.00")),
                entry("JE-002", debit("1000", "1.00"), line("4000", "0.00", "-1.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly(
                "JE-001 line 1 has a negative amount: debit -1.00, credit 0.00",
                "JE-002 line 2 has a negative amount: debit 0.00, credit -1.00");
    }
}

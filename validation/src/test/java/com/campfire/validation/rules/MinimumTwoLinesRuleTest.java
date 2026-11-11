package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MinimumTwoLinesRuleTest {

    private final MinimumTwoLinesRule rule = MinimumTwoLinesRule.INSTANCE;

    @Test
    void twoLinesAreValid() {
        var ledger = ledger(entry("JE-001", debit("1000", "1.00"), credit("4000", "1.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void oneLineFails() {
        var ledger = ledger(entry("JE-005", debit("1000", "1.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly("JE-005 has 1 line; an entry needs at least 2");
    }

    @Test
    void emptyEntryFails() {
        var ledger = ledger(entry("JE-005"));
        assertThat(rule.validate(ledger).errors()).containsExactly("JE-005 has 0 lines; an entry needs at least 2");
    }
}

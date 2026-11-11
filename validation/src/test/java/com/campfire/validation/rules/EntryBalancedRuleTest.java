package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static com.campfire.validation.rules.Ledgers.line;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EntryBalancedRuleTest {

    private final EntryBalancedRule rule = EntryBalancedRule.INSTANCE;

    @Test
    void balancedEntryIsValid() {
        var ledger = ledger(entry("JE-001", debit("1000", "9000.00"), credit("4000", "9000.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void sameAmountWithDifferentScaleIsBalanced() {
        var ledger = ledger(entry("JE-001", debit("1000", "100.0"), credit("4000", "100.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void splitCreditsThatSumToTheDebitAreBalanced() {
        var ledger = ledger(entry("JE-001", debit("1000", "100.00"), credit("4000", "60.00"), credit("6000", "40.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void unbalancedEntryFailsWithTotals() {
        var ledger = ledger(entry("JE-007", debit("6000", "9000.00"), credit("1000", "8999.00")));
        assertThat(rule.validate(ledger).errors())
                .containsExactly("JE-007 is not balanced: debits 9000.00, credits 8999.00");
    }

    @Test
    void reportsEveryUnbalancedEntry() {
        var ledger = ledger(
                entry("JE-001", debit("1000", "10.00"), credit("4000", "9.99")),
                entry("JE-002", debit("1000", "5.00"), credit("4000", "5.00")),
                entry("JE-003", debit("1000", "1.00"), credit("4000", "2.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly(
                "JE-001 is not balanced: debits 10.00, credits 9.99",
                "JE-003 is not balanced: debits 1.00, credits 2.00");
    }

    @Test
    void missingAmountCountsAsZero() {
        var ledger = ledger(entry("JE-001", line("1000", "10.00", null), line("4000", null, null)));
        assertThat(rule.validate(ledger).errors()).containsExactly("JE-001 is not balanced: debits 10.00, credits 0");
    }
}

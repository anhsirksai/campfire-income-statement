package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static com.campfire.validation.rules.Ledgers.line;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LineOneSidedRuleTest {

    private final LineOneSidedRule rule = LineOneSidedRule.INSTANCE;

    @Test
    void debitOnlyAndCreditOnlyLinesAreValid() {
        var ledger = ledger(entry("JE-001", debit("1000", "10.00"), credit("4000", "10.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void lineWithBothSidesFails() {
        var ledger = ledger(entry("JE-005", line("1000", "10.00", "10.00"), credit("4000", "0.01")));
        assertThat(rule.validate(ledger).errors()).containsExactly(
                "JE-005 line 1 must have exactly one of debit or credit: debit 10.00, credit 10.00");
    }

    @Test
    void lineWithNeitherSideFails() {
        var ledger = ledger(entry("JE-005", debit("1000", "10.00"), line("4000", "0.00", "0.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly(
                "JE-005 line 2 must have exactly one of debit or credit: debit 0.00, credit 0.00");
    }

    @Test
    void reportsEveryBadLineAcrossEntries() {
        var ledger = ledger(
                entry("JE-001", line("1000", "1.00", "1.00"), line("4000", null, null)),
                entry("JE-002", debit("1000", "1.00"), credit("4000", "1.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly(
                "JE-001 line 1 must have exactly one of debit or credit: debit 1.00, credit 1.00",
                "JE-001 line 2 must have exactly one of debit or credit: debit 0, credit 0");
    }
}

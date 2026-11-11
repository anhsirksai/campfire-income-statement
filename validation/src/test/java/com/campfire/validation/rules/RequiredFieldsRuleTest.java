package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.accounts;
import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static com.campfire.validation.rules.Ledgers.line;
import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.validation.api.LedgerSnapshot;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RequiredFieldsRuleTest {

    private final RequiredFieldsRule rule = RequiredFieldsRule.INSTANCE;

    @Test
    void completeLedgerIsValid() {
        var ledger = ledger(entry("JE-001", debit("1000", "10.00"), credit("4000", "10.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void missingCreditFailsEvenWhenTheEntryWouldBalance() {
        // The other rules treat null as 0, so debit 0.00 + missing credit "balances" against a 0.00 line.
        var ledger = ledger(entry("JE-005", line("1000", "10.00", "0.00"), line("4000", null, "10.00"),
                line("6000", "0.00", null)));
        assertThat(rule.validate(ledger).errors()).containsExactly(
                "JE-005 line 2 is missing a debit amount",
                "JE-005 line 3 is missing a credit amount");
    }

    @Test
    void missingIdAndDateFail() {
        var noId = new LedgerSnapshot.Entry(null, LocalDate.parse("2026-01-15"), "posted",
                List.of(debit("1000", "1.00"), credit("4000", "1.00")));
        var noDate = new LedgerSnapshot.Entry("JE-002", null, "posted",
                List.of(debit("1000", "1.00"), credit("4000", "1.00")));
        assertThat(rule.validate(ledger(noId, noDate)).errors()).containsExactly(
                "journal entry 1 is missing an id",
                "JE-002 is missing a date");
    }

    @Test
    void accountWithoutNumberFails() {
        var ledger = accounts(new LedgerSnapshot.Account(null, "asset", "balance_sheet"));
        assertThat(rule.validate(ledger).errors()).containsExactly("account 1 is missing a number");
    }
}

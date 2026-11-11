package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.validation.api.LedgerSnapshot;
import java.util.List;
import org.junit.jupiter.api.Test;

class UniqueIdsRuleTest {

    private final UniqueIdsRule rule = UniqueIdsRule.INSTANCE;

    @Test
    void distinctIdsAreValid() {
        var ledger = ledger(
                entry("JE-001", debit("1000", "1.00"), credit("4000", "1.00")),
                entry("JE-002", debit("1000", "1.00"), credit("4000", "1.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void duplicateEntryIdFails() {
        var ledger = ledger(
                entry("JE-001", debit("1000", "1.00"), credit("4000", "1.00")),
                entry("JE-001", debit("1000", "2.00"), credit("4000", "2.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly("journal entry JE-001 appears 2 times");
    }

    @Test
    void duplicateAccountNumberFails() {
        // accountNumbers() is a Set, so a second 4000 would otherwise disappear without a trace.
        var ledger = new LedgerSnapshot(List.of(
                new LedgerSnapshot.Account("4000", "revenue", "operating_revenue"),
                new LedgerSnapshot.Account("4000", "expense", "operating_expense")), List.of());
        assertThat(rule.validate(ledger).errors()).containsExactly("account 4000 appears 2 times");
    }
}

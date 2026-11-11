package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KnownStatusRuleTest {

    private final KnownStatusRule rule = KnownStatusRule.INSTANCE;

    @Test
    void postedDraftAndVoidAreValid() {
        var ledger = ledger(
                entry("JE-001", "posted", debit("1000", "1.00"), credit("4000", "1.00")),
                entry("JE-002", "draft", debit("1000", "1.00"), credit("4000", "1.00")),
                entry("JE-003", "void", debit("1000", "1.00"), credit("4000", "1.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void unknownStatusFails() {
        var ledger = ledger(entry("JE-005", "pending", debit("1000", "1.00"), credit("4000", "1.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly("JE-005 has unknown status 'pending'");
    }

    @Test
    void statusIsCaseSensitive() {
        // The data and the SQL filter use lowercase codes; "Posted" would not match status = 'posted'.
        var ledger = ledger(entry("JE-005", "Posted", debit("1000", "1.00"), credit("4000", "1.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly("JE-005 has unknown status 'Posted'");
    }

    @Test
    void missingStatusFails() {
        var ledger = ledger(entry("JE-005", (String) null, debit("1000", "1.00"), credit("4000", "1.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly("JE-005 has unknown status 'null'");
    }
}

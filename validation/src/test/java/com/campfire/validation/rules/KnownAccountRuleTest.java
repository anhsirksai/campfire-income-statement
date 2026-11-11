package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.credit;
import static com.campfire.validation.rules.Ledgers.debit;
import static com.campfire.validation.rules.Ledgers.entry;
import static com.campfire.validation.rules.Ledgers.ledger;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KnownAccountRuleTest {

    private final KnownAccountRule rule = KnownAccountRule.INSTANCE;

    @Test
    void knownAccountsAreValid() {
        var ledger = ledger(entry("JE-001", debit("1000", "10.00"), credit("4000", "10.00")));
        assertThat(rule.validate(ledger).isValid()).isTrue();
    }

    @Test
    void unknownAccountFails() {
        var ledger = ledger(entry("JE-005", debit("1000", "10.00"), credit("9999", "10.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly("JE-005 line 2 uses unknown account 9999");
    }

    @Test
    void missingAccountFails() {
        var ledger = ledger(entry("JE-005", debit("1000", "10.00"), credit(null, "10.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly("JE-005 line 2 uses unknown account null");
    }

    @Test
    void reportsEveryUnknownAccount() {
        var ledger = ledger(
                entry("JE-001", debit("1001", "10.00"), credit("4000", "10.00")),
                entry("JE-002", debit("1000", "10.00"), credit("4001", "10.00")));
        assertThat(rule.validate(ledger).errors()).containsExactly(
                "JE-001 line 1 uses unknown account 1001",
                "JE-002 line 2 uses unknown account 4001");
    }
}

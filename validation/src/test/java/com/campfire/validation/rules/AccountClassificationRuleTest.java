package com.campfire.validation.rules;

import static com.campfire.validation.rules.Ledgers.accounts;
import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.validation.api.LedgerSnapshot;
import org.junit.jupiter.api.Test;

class AccountClassificationRuleTest {

    private final AccountClassificationRule rule = AccountClassificationRule.INSTANCE;

    @Test
    void matchingTypeAndSubtypePass() {
        assertThat(rule.validate(Ledgers.ledger()).isValid()).isTrue();
    }

    @Test
    void expenseTypedAccountInRevenueFails() {
        var result = rule.validate(accounts(new LedgerSnapshot.Account("4000", "expense", "operating_revenue")));
        assertThat(result.errors()).containsExactly("account 4000 has type expense but subtype operating_revenue");
    }
}

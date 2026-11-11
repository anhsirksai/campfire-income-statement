package com.campfire.ledger.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.ledger.model.AccountType;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NormalBalanceStrategyFactoryTest {

    private static final BigDecimal DEBIT = new BigDecimal("100.00");
    private static final BigDecimal CREDIT = new BigDecimal("30.00");

    @ParameterizedTest
    @CsvSource({
            "ASSET,     70.00",   // debit-normal: debit − credit
            "EXPENSE,   70.00",
            "LIABILITY, -70.00",  // credit-normal: credit − debit
            "EQUITY,    -70.00",
            "REVENUE,   -70.00",  // e.g. contra revenue with net debits comes out negative
    })
    void signFollowsNormalBalance(AccountType type, BigDecimal expected) {
        assertThat(NormalBalanceStrategyFactory.forType(type).balance(DEBIT, CREDIT)).isEqualTo(expected);
    }
}

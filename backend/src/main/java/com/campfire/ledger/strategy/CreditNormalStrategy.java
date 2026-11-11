package com.campfire.ledger.strategy;

import java.math.BigDecimal;

/**
 * Liabilities, equity, and revenue: credits increase the balance.
 * A contra-revenue account (type revenue) with net debits therefore comes out negative, which is
 * exactly how it should reduce revenue.
 */
public final class CreditNormalStrategy implements NormalBalanceStrategy {

    static final CreditNormalStrategy INSTANCE = new CreditNormalStrategy();

    private CreditNormalStrategy() {
    }

    @Override
    public BigDecimal balance(BigDecimal totalDebit, BigDecimal totalCredit) {
        return totalCredit.subtract(totalDebit);
    }
}

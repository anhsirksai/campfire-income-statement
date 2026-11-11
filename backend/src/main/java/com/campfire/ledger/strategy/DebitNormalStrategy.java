package com.campfire.ledger.strategy;

import java.math.BigDecimal;

/** Assets and expenses: debits increase the balance. */
public final class DebitNormalStrategy implements NormalBalanceStrategy {

    static final DebitNormalStrategy INSTANCE = new DebitNormalStrategy();

    private DebitNormalStrategy() {
    }

    @Override
    public BigDecimal balance(BigDecimal totalDebit, BigDecimal totalCredit) {
        return totalDebit.subtract(totalCredit);
    }
}

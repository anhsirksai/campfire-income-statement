package com.campfire.ledger.strategy;

import java.math.BigDecimal;

/**
 * Turns raw debit/credit totals into a signed balance, positive in the account's normal direction.
 * Pick one with {@link NormalBalanceStrategyFactory#forType}.
 */
public interface NormalBalanceStrategy {

    BigDecimal balance(BigDecimal totalDebit, BigDecimal totalCredit);
}

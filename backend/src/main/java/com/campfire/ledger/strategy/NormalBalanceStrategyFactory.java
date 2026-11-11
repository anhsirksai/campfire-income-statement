package com.campfire.ledger.strategy;

import com.campfire.ledger.model.AccountType;

/** Chooses the normal-balance strategy for an account type. */
public final class NormalBalanceStrategyFactory {

    private NormalBalanceStrategyFactory() {
    }

    public static NormalBalanceStrategy forType(AccountType type) {
        return switch (type) {
            case ASSET, EXPENSE -> DebitNormalStrategy.INSTANCE;
            case LIABILITY, EQUITY, REVENUE -> CreditNormalStrategy.INSTANCE;
        };
    }
}

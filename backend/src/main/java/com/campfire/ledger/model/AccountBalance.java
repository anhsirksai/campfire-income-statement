package com.campfire.ledger.model;

import java.math.BigDecimal;

/** Raw debit and credit totals for one account over a period. Signs are applied by a NormalBalanceStrategy. */
public record AccountBalance(Account account, BigDecimal totalDebit, BigDecimal totalCredit) {
}

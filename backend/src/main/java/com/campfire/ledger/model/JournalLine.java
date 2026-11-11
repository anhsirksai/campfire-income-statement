package com.campfire.ledger.model;

import java.math.BigDecimal;

/** One debit or credit to one account. One of debit/credit is 0.00. */
public record JournalLine(String accountNumber, BigDecimal debit, BigDecimal credit) {
}

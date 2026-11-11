package com.campfire.validation.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * The ledger as read from ledger.json, before it is inserted into the database.
 * Plain data so the validation module does not depend on backend classes.
 */
public record LedgerSnapshot(Set<String> accountNumbers, List<Entry> entries) {

    public record Entry(String id, LocalDate date, String status, List<Line> lines) {
    }

    public record Line(String account, BigDecimal debit, BigDecimal credit) {
    }
}

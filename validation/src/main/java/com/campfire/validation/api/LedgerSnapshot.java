package com.campfire.validation.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The ledger as read from ledger.json, before it is inserted into the database.
 * Plain data so the validation module does not depend on backend classes.
 */
public record LedgerSnapshot(List<Account> accounts, List<Entry> entries) {

    /** Type and subtype are the lowercase codes from ledger.json, unparsed, so rules can report bad values. */
    public record Account(String number, String type, String subtype) {
    }

    public record Entry(String id, LocalDate date, String status, List<Line> lines) {
    }

    public record Line(String account, BigDecimal debit, BigDecimal credit) {
    }

    public Set<String> accountNumbers() {
        return accounts.stream().map(Account::number).collect(Collectors.toSet());
    }
}

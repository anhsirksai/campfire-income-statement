package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Tiny builders for hand-made ledgers in rule tests. Amounts are strings so scale is exactly what the test says. */
final class Ledgers {

    static final Set<String> ACCOUNTS = Set.of("1000", "4000", "6000");

    private Ledgers() {
    }

    static LedgerSnapshot ledger(LedgerSnapshot.Entry... entries) {
        return new LedgerSnapshot(ACCOUNTS, List.of(entries));
    }

    static LedgerSnapshot.Entry entry(String id, LedgerSnapshot.Line... lines) {
        return entry(id, "posted", lines);
    }

    static LedgerSnapshot.Entry entry(String id, String status, LedgerSnapshot.Line... lines) {
        return new LedgerSnapshot.Entry(id, LocalDate.parse("2026-01-15"), status, List.of(lines));
    }

    static LedgerSnapshot.Line debit(String account, String amount) {
        return new LedgerSnapshot.Line(account, new BigDecimal(amount), new BigDecimal("0.00"));
    }

    static LedgerSnapshot.Line credit(String account, String amount) {
        return new LedgerSnapshot.Line(account, new BigDecimal("0.00"), new BigDecimal(amount));
    }

    static LedgerSnapshot.Line line(String account, String debit, String credit) {
        return new LedgerSnapshot.Line(account, amount(debit), amount(credit));
    }

    private static BigDecimal amount(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}

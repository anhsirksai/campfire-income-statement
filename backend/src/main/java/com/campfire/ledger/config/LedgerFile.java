package com.campfire.ledger.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The shape of data/ledger.json, field for field. Only {@link LedgerDataLoader} uses it.
 * Amounts arrive as decimal strings and Jackson parses them straight into BigDecimal.
 */
record LedgerFile(
        String company,
        String currency,
        List<AccountJson> accounts,
        @JsonProperty("journal_entries") List<EntryJson> journalEntries) {

    record AccountJson(
            String number,
            String name,
            String type,
            String subtype,
            @JsonProperty("is_active") boolean isActive) {
    }

    record EntryJson(String id, LocalDate date, String status, String memo, List<LineJson> lines) {
    }

    record LineJson(String account, BigDecimal debit, BigDecimal credit) {
    }
}

package com.campfire.ledger.model;

import java.util.Locale;

/** Only POSTED entries are in the books. DRAFT is awaiting approval, VOID was cancelled. */
public enum EntryStatus {
    POSTED,
    DRAFT,
    VOID;

    public static EntryStatus fromCode(String code) {
        return valueOf(code.toUpperCase(Locale.ROOT));
    }

    public String code() {
        return name().toLowerCase(Locale.ROOT);
    }
}

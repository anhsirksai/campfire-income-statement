package com.campfire.ledger.model;

/** Only POSTED entries are in the books. DRAFT is awaiting approval, VOID was cancelled. */
public enum EntryStatus {
    POSTED,
    DRAFT,
    VOID;

    public static EntryStatus fromCode(String code) {
        return valueOf(code.toUpperCase());
    }

    public String code() {
        return name().toLowerCase();
    }
}

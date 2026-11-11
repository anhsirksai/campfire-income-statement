package com.campfire.ledger.model;

/** Account subtype from the chart of accounts. Decides which statement section an account lands in. */
public enum AccountSubtype {
    OPERATING_REVENUE,
    CONTRA_REVENUE,
    COGS,
    OPERATING_EXPENSE,
    OTHER_INCOME,
    BALANCE_SHEET;

    /** Maps the lowercase value used in ledger.json and the database. */
    public static AccountSubtype fromCode(String code) {
        return valueOf(code.toUpperCase());
    }
}

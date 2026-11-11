package com.campfire.ledger.model;

/** Account type from the chart of accounts. Decides the normal balance (see strategy package). */
public enum AccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE;

    /** Maps the lowercase value used in ledger.json and the database. */
    public static AccountType fromCode(String code) {
        return valueOf(code.toUpperCase());
    }
}

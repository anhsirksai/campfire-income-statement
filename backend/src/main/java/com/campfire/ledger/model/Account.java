package com.campfire.ledger.model;

/** One row of the chart of accounts. */
public record Account(String number, String name, AccountType type, AccountSubtype subtype, boolean active) {
}

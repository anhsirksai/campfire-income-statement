package com.campfire.ledger.model;

/** The entity whose books these are. One per ledger.json. */
public record Company(String name, String currency) {
}

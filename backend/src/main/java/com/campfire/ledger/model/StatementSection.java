package com.campfire.ledger.model;

import java.util.Optional;

/** Income statement sections, in display order. Keys match the Section.key enum in api/openapi.yaml. */
public enum StatementSection {
    REVENUE("Revenue"),
    COGS("Cost of goods sold"),
    OPERATING_EXPENSES("Operating expenses"),
    OTHER_INCOME("Other income");

    private final String title;

    StatementSection(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    /** Empty for balance-sheet accounts, which are not on the income statement. */
    public static Optional<StatementSection> fromSubtype(AccountSubtype subtype) {
        return switch (subtype) {
            case OPERATING_REVENUE, CONTRA_REVENUE -> Optional.of(REVENUE);
            case COGS -> Optional.of(COGS);
            case OPERATING_EXPENSE -> Optional.of(OPERATING_EXPENSES);
            case OTHER_INCOME -> Optional.of(OTHER_INCOME);
            case BALANCE_SHEET -> Optional.empty();
        };
    }
}

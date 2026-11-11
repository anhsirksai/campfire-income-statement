package com.campfire.ledger.model;

import java.time.LocalDate;
import java.util.List;

/** One recorded business event. Lines are in their original order. */
public record JournalEntry(String id, LocalDate date, EntryStatus status, String memo, List<JournalLine> lines) {
}

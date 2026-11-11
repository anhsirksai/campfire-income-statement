package com.campfire.ledger.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.rules.ValidationRules;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The shipped data/ledger.json must pass every ledger rule, or the app would refuse to start.
 * Builds the snapshot here with plain Jackson so it does not depend on Track A's loader.
 */
class RealLedgerRulesTest {

    @Test
    void realLedgerPassesAllLedgerRules() throws Exception {
        LedgerSnapshot snapshot = readLedger(Path.of("data/ledger.json"));
        assertThat(snapshot.entries()).hasSize(25);

        List<String> errors = new ValidationRegistry(ValidationRules.all()).validate(snapshot);

        assertThat(errors).isEmpty();
    }

    private static LedgerSnapshot readLedger(Path file) throws Exception {
        JsonNode root = new ObjectMapper().readTree(file.toFile());
        Set<String> accountNumbers = new HashSet<>();
        for (JsonNode account : root.get("accounts")) {
            accountNumbers.add(account.get("number").asText());
        }
        List<LedgerSnapshot.Entry> entries = new ArrayList<>();
        for (JsonNode entry : root.get("journal_entries")) {
            List<LedgerSnapshot.Line> lines = new ArrayList<>();
            for (JsonNode line : entry.get("lines")) {
                // Amounts are JSON strings; parse straight to BigDecimal so nothing passes through double.
                lines.add(new LedgerSnapshot.Line(line.get("account").asText(),
                        new BigDecimal(line.get("debit").asText()), new BigDecimal(line.get("credit").asText())));
            }
            entries.add(new LedgerSnapshot.Entry(entry.get("id").asText(),
                    LocalDate.parse(entry.get("date").asText()), entry.get("status").asText(), lines));
        }
        return new LedgerSnapshot(accountNumbers, entries);
    }
}

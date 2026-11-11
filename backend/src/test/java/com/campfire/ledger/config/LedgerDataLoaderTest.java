package com.campfire.ledger.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.campfire.ledger.dao.AccountDao;
import com.campfire.ledger.dao.CompanyDao;
import com.campfire.ledger.dao.JournalEntryDao;
import com.campfire.ledger.dao.JournalLineDao;
import com.campfire.ledger.exception.LedgerDataException;
import com.campfire.ledger.validation.ValidationRegistry;
import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Loader behaviour without Spring: parsing, the hand-off to validation rules, and refusing bad data. */
class LedgerDataLoaderTest {

    private static final ObjectMapper JSON = JsonMapper.builder().findAndAddModules().build();

    private static final String UNBALANCED = """
            {"company": "Test Co", "currency": "USD",
             "accounts": [
               {"number": "1000", "name": "Cash", "type": "asset", "subtype": "balance_sheet", "is_active": true},
               {"number": "4000", "name": "Sales", "type": "revenue", "subtype": "operating_revenue", "is_active": true}],
             "journal_entries": [
               {"id": "JE-1", "date": "2026-01-05", "status": "posted", "memo": "off by a cent", "lines": [
                 {"account": "1000", "debit": "100.00", "credit": "0.00"},
                 {"account": "4000", "debit": "0.00", "credit": "99.99"}]}]}
            """;

    private final CompanyDao companyDao = mock(CompanyDao.class);
    private final AccountDao accountDao = mock(AccountDao.class);
    private final JournalEntryDao journalEntryDao = mock(JournalEntryDao.class);
    private final JournalLineDao journalLineDao = mock(JournalLineDao.class);

    /** Stand-in for the plugin's EntryBalancedRule, so this test does not depend on the validation track. */
    private static final ValidationRule<LedgerSnapshot> BALANCED = new ValidationRule<>() {
        @Override
        public Class<LedgerSnapshot> target() {
            return LedgerSnapshot.class;
        }

        @Override
        public ValidationResult validate(LedgerSnapshot input) {
            return ValidationResult.of(input.entries().stream()
                    .filter(e -> sum(e, true).compareTo(sum(e, false)) != 0)
                    .map(e -> e.id() + " is not balanced")
                    .toList());
        }

        private BigDecimal sum(LedgerSnapshot.Entry e, boolean debit) {
            return e.lines().stream().map(l -> debit ? l.debit() : l.credit()).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    };

    private LedgerDataLoader loader(Path file, ValidationRule<?>... rules) {
        return new LedgerDataLoader(JSON, new ValidationRegistry(List.of(rules)),
                companyDao, accountDao, journalEntryDao, journalLineDao, file);
    }

    @Test
    void failingLedgerRuleStopsStartupBeforeAnyInsert(@TempDir Path dir) throws Exception {
        Path file = Files.writeString(dir.resolve("ledger.json"), UNBALANCED);

        assertThatThrownBy(() -> loader(file, BALANCED).run(null))
                .isInstanceOf(LedgerDataException.class)
                .hasMessageContaining("JE-1 is not balanced");
        verify(companyDao, never()).insert(any());
        verify(journalLineDao, never()).insert(any(), anyInt(), any());
    }

    @Test
    void missingFileIsALedgerDataException(@TempDir Path dir) {
        assertThatThrownBy(() -> loader(dir.resolve("nope.json")).run(null))
                .isInstanceOf(LedgerDataException.class)
                .hasMessageContaining("Could not read ledger file");
    }

    @Test
    void realLedgerParsesWithExactAmounts() {
        AtomicReference<LedgerSnapshot> seen = new AtomicReference<>();
        ValidationRule<LedgerSnapshot> capture = new ValidationRule<>() {
            @Override
            public Class<LedgerSnapshot> target() {
                return LedgerSnapshot.class;
            }

            @Override
            public ValidationResult validate(LedgerSnapshot input) {
                seen.set(input);
                return ValidationResult.ok();
            }
        };

        loader(Path.of("data/ledger.json"), capture, BALANCED).run(null);

        LedgerSnapshot snapshot = seen.get();
        assertThat(snapshot.accountNumbers()).hasSize(15);
        assertThat(snapshot.entries()).hasSize(25);
        LedgerSnapshot.Entry je002 = snapshot.entries().get(1);
        assertThat(je002.id()).isEqualTo("JE-002");
        assertThat(je002.lines().get(0).debit()).isEqualTo(new BigDecimal("12450.75"));
    }
}

package com.campfire.ledger.dao;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/** The database is a second guard behind the validation rules (spec §4.1): bad rows are rejected even on a direct insert. */
@SpringBootTest
class SchemaConstraintsTest {

    @Autowired
    private JdbcTemplate jdbc;

    private void insertLine(int lineNo, String debit, String credit) {
        jdbc.update("INSERT INTO journal_lines (entry_id, line_no, account_number, debit, credit) VALUES ('JE-002', ?, '4000', ?, ?)",
                lineNo, new BigDecimal(debit), new BigDecimal(credit));
    }

    @Test
    void rejectsLineWithBothSides() {
        assertThatThrownBy(() -> insertLine(90, "10.00", "10.00")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> insertLine(91, "-10.00", "0.00")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateLinePosition() {
        assertThatThrownBy(() -> insertLine(1, "10.00", "0.00")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsUnknownStatus() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO journal_entries (id, entry_date, status) VALUES ('JE-X', DATE '2026-01-01', 'pending')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

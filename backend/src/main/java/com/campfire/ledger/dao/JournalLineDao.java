package com.campfire.ledger.dao;

import com.campfire.ledger.dao.mapper.AccountBalanceRowMapper;
import com.campfire.ledger.model.AccountBalance;
import com.campfire.ledger.model.EntryStatus;
import com.campfire.ledger.model.JournalLine;
import com.campfire.ledger.query.LedgerQueries;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JournalLineDao {

    private final NamedParameterJdbcTemplate jdbc;

    public JournalLineDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** lineNo is the line's position within its entry, starting at 1. */
    public void insert(String entryId, int lineNo, JournalLine line) {
        jdbc.update(LedgerQueries.INSERT_JOURNAL_LINE, Map.of(
                "entryId", entryId,
                "lineNo", lineNo,
                "accountNumber", line.accountNumber(),
                "debit", line.debit(),
                "credit", line.credit()));
    }

    /** Debit/credit totals per account for entries with this status and start <= date <= end. */
    public List<AccountBalance> sumByAccount(EntryStatus status, LocalDate start, LocalDate end) {
        return jdbc.query(LedgerQueries.SUM_LINES_BY_ACCOUNT, Map.of(
                "status", status.code(),
                "start", start,
                "end", end), AccountBalanceRowMapper.INSTANCE);
    }
}

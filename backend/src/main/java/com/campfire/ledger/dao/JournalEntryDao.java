package com.campfire.ledger.dao;

import com.campfire.ledger.model.JournalEntry;
import com.campfire.ledger.query.LedgerQueries;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JournalEntryDao {

    private final NamedParameterJdbcTemplate jdbc;

    public JournalEntryDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Inserts the entry header only. Lines go through {@link JournalLineDao}. */
    public void insert(JournalEntry entry) {
        Map<String, Object> params = new HashMap<>();   // memo may be null, which Map.of rejects
        params.put("id", entry.id());
        params.put("entryDate", entry.date());
        params.put("status", entry.status().code());
        params.put("memo", entry.memo());
        jdbc.update(LedgerQueries.INSERT_JOURNAL_ENTRY, params);
    }
}

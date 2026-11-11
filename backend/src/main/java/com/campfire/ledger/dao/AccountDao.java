package com.campfire.ledger.dao;

import com.campfire.ledger.model.Account;
import com.campfire.ledger.query.LedgerQueries;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountDao {

    private final NamedParameterJdbcTemplate jdbc;

    public AccountDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Type and subtype are stored as the lowercase codes used in ledger.json. */
    public void insert(Account account) {
        jdbc.update(LedgerQueries.INSERT_ACCOUNT, Map.of(
                "number", account.number(),
                "name", account.name(),
                "type", account.type().name().toLowerCase(),
                "subtype", account.subtype().name().toLowerCase(),
                "isActive", account.active()));
    }
}

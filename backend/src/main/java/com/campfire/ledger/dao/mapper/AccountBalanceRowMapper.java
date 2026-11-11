package com.campfire.ledger.dao.mapper;

import com.campfire.ledger.model.Account;
import com.campfire.ledger.model.AccountBalance;
import com.campfire.ledger.model.AccountSubtype;
import com.campfire.ledger.model.AccountType;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/** Maps a row of LedgerQueries.SUM_LINES_BY_ACCOUNT. Amounts are read as BigDecimal, never double. */
public enum AccountBalanceRowMapper implements RowMapper<AccountBalance> {
    INSTANCE;

    @Override
    public AccountBalance mapRow(ResultSet rs, int rowNum) throws SQLException {
        Account account = new Account(
                rs.getString("number"),
                rs.getString("name"),
                AccountType.fromCode(rs.getString("type")),
                AccountSubtype.fromCode(rs.getString("subtype")),
                rs.getBoolean("is_active"));
        return new AccountBalance(account, rs.getBigDecimal("total_debit"), rs.getBigDecimal("total_credit"));
    }
}

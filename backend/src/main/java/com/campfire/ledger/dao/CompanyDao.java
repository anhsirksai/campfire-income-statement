package com.campfire.ledger.dao;

import com.campfire.ledger.dao.mapper.CompanyRowMapper;
import com.campfire.ledger.model.Company;
import com.campfire.ledger.query.LedgerQueries;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CompanyDao {

    private final NamedParameterJdbcTemplate jdbc;

    public CompanyDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Company company) {
        jdbc.update(LedgerQueries.INSERT_COMPANY, Map.of(
                "name", company.name(),
                "currency", company.currency()));
    }

    /** The ledger holds exactly one company. */
    public Company find() {
        return jdbc.queryForObject(LedgerQueries.SELECT_COMPANY, Map.of(), CompanyRowMapper.INSTANCE);
    }
}

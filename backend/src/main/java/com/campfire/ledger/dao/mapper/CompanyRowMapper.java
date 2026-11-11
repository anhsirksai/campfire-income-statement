package com.campfire.ledger.dao.mapper;

import com.campfire.ledger.model.Company;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public enum CompanyRowMapper implements RowMapper<Company> {
    INSTANCE;

    @Override
    public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Company(rs.getString("name"), rs.getString("currency"));
    }
}

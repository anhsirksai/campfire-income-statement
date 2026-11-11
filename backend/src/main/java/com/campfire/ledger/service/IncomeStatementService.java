package com.campfire.ledger.service;

import com.campfire.ledger.model.dto.IncomeStatementResponse;
import java.time.LocalDate;

public interface IncomeStatementService {

    /** Income statement for POSTED entries with start <= date <= end. Dates are already validated. */
    IncomeStatementResponse getIncomeStatement(LocalDate start, LocalDate end);
}

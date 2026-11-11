package com.campfire.ledger.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Response body of GET /income-statement. Must match IncomeStatement in api/openapi.yaml. */
public record IncomeStatementResponse(
        String company,
        String currency,
        LocalDate start,
        LocalDate end,
        List<SectionDto> sections,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal grossProfit,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal operatingIncome,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal netIncome) {
}

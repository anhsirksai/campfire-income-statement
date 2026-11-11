package com.campfire.ledger;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** HTTP-level checks: the JSON matches api/openapi.yaml, and bad input is a 400. Numbers are covered in IncomeStatementServiceTest. */
@SpringBootTest
@AutoConfigureMockMvc
class IncomeStatementControllerTest {

    private static final String MONEY = "^-?\\d+\\.\\d{2}$";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void q1ResponseMatchesContract() throws Exception {
        mockMvc.perform(get("/income-statement").param("start", "2026-01-01").param("end", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Northwind Coffee Roasters"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.start").value("2026-01-01"))
                .andExpect(jsonPath("$.end").value("2026-03-31"))
                .andExpect(jsonPath("$.sections.length()").value(4))
                .andExpect(jsonPath("$.sections[*].key")
                        .value(contains("REVENUE", "COGS", "OPERATING_EXPENSES", "OTHER_INCOME")))
                .andExpect(jsonPath("$.sections[0].title").value("Revenue"))
                .andExpect(jsonPath("$.sections[0].lines[2].accountNumber").value("4900"))
                .andExpect(jsonPath("$.sections[0].lines[2].accountName").value("Sales Returns & Discounts"))
                // Amounts are JSON strings, not numbers: value("...") on a number would fail.
                .andExpect(jsonPath("$.sections[0].lines[2].amount").value("-800.25"))
                .andExpect(jsonPath("$.sections[0].total").value("37850.50"))
                .andExpect(jsonPath("$.grossProfit").value("23577.75"))
                .andExpect(jsonPath("$.operatingIncome").value("-44522.32"))
                .andExpect(jsonPath("$.netIncome").value("-44480.14"))
                .andExpect(jsonPath("$.sections[*].total").value(everyItem(matchesPattern(MONEY))))
                .andExpect(jsonPath("$.sections[*].lines[*].amount").value(everyItem(matchesPattern(MONEY))));
    }

    @Test
    void emptySectionsAreStillPresentWithZeroTotals() throws Exception {
        mockMvc.perform(get("/income-statement").param("start", "2025-12-01").param("end", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sections.length()").value(4))
                .andExpect(jsonPath("$.sections[1].lines").isEmpty())
                .andExpect(jsonPath("$.sections[1].total").value("0.00"))
                .andExpect(jsonPath("$.netIncome").value("5000.00"));
    }

    @Test
    void startAfterEndIsBadRequest() throws Exception {
        mockMvc.perform(get("/income-statement").param("start", "2026-04-01").param("end", "2026-03-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("start must be on or before end"));
    }

    @Test
    void malformedDateIsBadRequest() throws Exception {
        mockMvc.perform(get("/income-statement").param("start", "2026-13-01").param("end", "2026-03-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("start must be a date in YYYY-MM-DD format"));
    }
}

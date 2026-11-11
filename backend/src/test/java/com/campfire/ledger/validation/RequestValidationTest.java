package com.campfire.ledger.validation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Missing dates are rejected with 400 by DateRangePresentRule, before the service runs (spec D8). */
@SpringBootTest
@AutoConfigureMockMvc
class RequestValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void missingStartIsBadRequest() throws Exception {
        mockMvc.perform(get("/income-statement").param("end", "2026-03-31"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]").value("start is required"));
    }

    @Test
    void missingBothDatesReportsBoth() throws Exception {
        mockMvc.perform(get("/income-statement"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("start is required"))
                .andExpect(jsonPath("$.errors[1]").value("end is required"));
    }
}

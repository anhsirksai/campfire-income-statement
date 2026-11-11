package com.campfire.validation.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.validation.api.ReportPeriodInput;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateRangePresentRuleTest {

    private final DateRangePresentRule rule = DateRangePresentRule.INSTANCE;
    private final LocalDate day = LocalDate.parse("2026-03-31");

    @Test
    void bothDatesPresentIsValid() {
        assertThat(rule.validate(new ReportPeriodInput(day, day)).isValid()).isTrue();
    }

    @Test
    void missingStartFails() {
        assertThat(rule.validate(new ReportPeriodInput(null, day)).errors()).containsExactly("start is required");
    }

    @Test
    void missingEndFails() {
        assertThat(rule.validate(new ReportPeriodInput(day, null)).errors()).containsExactly("end is required");
    }

    @Test
    void bothMissingReportsBoth() {
        assertThat(rule.validate(new ReportPeriodInput(null, null)).errors())
                .containsExactly("start is required", "end is required");
    }
}

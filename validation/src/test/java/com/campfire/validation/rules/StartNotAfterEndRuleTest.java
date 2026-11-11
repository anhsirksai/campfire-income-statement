package com.campfire.validation.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.validation.api.ReportPeriodInput;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class StartNotAfterEndRuleTest {

    private final StartNotAfterEndRule rule = StartNotAfterEndRule.INSTANCE;

    @Test
    void singleDayRangeIsValid() {
        LocalDate day = LocalDate.parse("2026-03-31");
        assertThat(rule.validate(new ReportPeriodInput(day, day)).isValid()).isTrue();
    }

    @Test
    void startAfterEndFails() {
        var result = rule.validate(new ReportPeriodInput(LocalDate.parse("2026-04-01"), LocalDate.parse("2026-03-31")));
        assertThat(result.errors()).containsExactly("start must be on or before end");
    }
}

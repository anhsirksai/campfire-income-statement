package com.campfire.validation.rules;

import com.campfire.validation.api.ReportPeriodInput;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;

/** The range is inclusive, so start == end (a single day) is valid. Missing dates are another rule's job. */
public enum StartNotAfterEndRule implements ValidationRule<ReportPeriodInput> {
    INSTANCE;

    @Override
    public Class<ReportPeriodInput> target() {
        return ReportPeriodInput.class;
    }

    @Override
    public ValidationResult validate(ReportPeriodInput input) {
        if (input.start() == null || input.end() == null) {
            return ValidationResult.ok();
        }
        if (input.start().isAfter(input.end())) {
            return ValidationResult.fail("start must be on or before end");
        }
        return ValidationResult.ok();
    }
}

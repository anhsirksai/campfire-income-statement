package com.campfire.validation.rules;

import com.campfire.validation.api.ReportPeriodInput;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.util.ArrayList;
import java.util.List;

/** Both dates are required: a missing date is an error, not "all time". Reports both if both are missing. */
public enum DateRangePresentRule implements ValidationRule<ReportPeriodInput> {
    INSTANCE;

    @Override
    public Class<ReportPeriodInput> target() {
        return ReportPeriodInput.class;
    }

    @Override
    public ValidationResult validate(ReportPeriodInput input) {
        List<String> errors = new ArrayList<>();
        if (input.start() == null) {
            errors.add("start is required");
        }
        if (input.end() == null) {
            errors.add("end is required");
        }
        return ValidationResult.of(errors);
    }
}

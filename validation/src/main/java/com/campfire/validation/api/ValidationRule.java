package com.campfire.validation.api;

/**
 * One validation check. Implementations live in the {@code rules} package and are stateless
 * singletons (an enum with a single INSTANCE). {@code ValidationRules.all()} lists them.
 *
 * @param <T> the input type this rule checks, e.g. {@link ReportPeriodInput} or {@link LedgerSnapshot}
 */
public interface ValidationRule<T> {

    /** The input type this rule applies to. The registry uses it to pick rules for an input. */
    Class<T> target();

    ValidationResult validate(T input);

    /** Readable id for logs. Not name(), which enums already define as "INSTANCE". */
    default String id() {
        return getClass().getSimpleName();
    }
}

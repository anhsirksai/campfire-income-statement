package com.campfire.validation.rules;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Entry ids and account numbers are primary keys. A duplicate entry is usually a double-posting, and a
 * duplicate account would hide behind accountNumbers() (a Set) until the insert fails.
 */
public enum UniqueIdsRule implements ValidationRule<LedgerSnapshot> {
    INSTANCE;

    @Override
    public Class<LedgerSnapshot> target() {
        return LedgerSnapshot.class;
    }

    @Override
    public ValidationResult validate(LedgerSnapshot input) {
        List<String> errors = new ArrayList<>();
        countOf(input.accounts().stream().map(LedgerSnapshot.Account::number).toList()).forEach((number, count) -> {
            if (count > 1) {
                errors.add("account " + number + " appears " + count + " times");
            }
        });
        countOf(input.entries().stream().map(LedgerSnapshot.Entry::id).toList()).forEach((id, count) -> {
            if (count > 1) {
                errors.add("journal entry " + id + " appears " + count + " times");
            }
        });
        return ValidationResult.of(errors);
    }

    /** Occurrences per id, in first-seen order. Null ids are RequiredFieldsRule's job, so they are skipped. */
    private static Map<String, Long> countOf(List<String> ids) {
        return ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
    }
}

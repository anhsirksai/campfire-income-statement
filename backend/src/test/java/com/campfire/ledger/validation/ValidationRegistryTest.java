package com.campfire.ledger.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.validation.api.LedgerSnapshot;
import com.campfire.validation.api.ReportPeriodInput;
import com.campfire.validation.api.ValidationResult;
import com.campfire.validation.api.ValidationRule;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationRegistryTest {

    private final ValidationRegistry registry = new ValidationRegistry(List.of(
            FailingPeriodRuleA.INSTANCE, FailingLedgerRule.INSTANCE, FailingPeriodRuleB.INSTANCE));

    @Test
    void runsOnlyRulesForTheInputTypeAndCollectsAllTheirErrors() {
        assertThat(registry.validate(new ReportPeriodInput(null, null)))
                .containsExactly("period error A", "period error B");
    }

    @Test
    void ledgerInputRunsOnlyLedgerRules() {
        assertThat(registry.validate(new LedgerSnapshot(List.of(), List.of())))
                .containsExactly("ledger error");
    }

    @Test
    void inputWithNoMatchingRulesIsValid() {
        assertThat(registry.validate("not a known input")).isEmpty();
    }

    enum FailingPeriodRuleA implements ValidationRule<ReportPeriodInput> {
        INSTANCE;

        @Override
        public Class<ReportPeriodInput> target() {
            return ReportPeriodInput.class;
        }

        @Override
        public ValidationResult validate(ReportPeriodInput input) {
            return ValidationResult.fail("period error A");
        }
    }

    enum FailingPeriodRuleB implements ValidationRule<ReportPeriodInput> {
        INSTANCE;

        @Override
        public Class<ReportPeriodInput> target() {
            return ReportPeriodInput.class;
        }

        @Override
        public ValidationResult validate(ReportPeriodInput input) {
            return ValidationResult.fail("period error B");
        }
    }

    enum FailingLedgerRule implements ValidationRule<LedgerSnapshot> {
        INSTANCE;

        @Override
        public Class<LedgerSnapshot> target() {
            return LedgerSnapshot.class;
        }

        @Override
        public ValidationResult validate(LedgerSnapshot input) {
            return ValidationResult.fail("ledger error");
        }
    }
}

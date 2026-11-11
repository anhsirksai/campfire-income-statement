---
name: add-validation-rule
description: Add a new validation rule to the validation module (request or ledger-data rule). Use when a new check on dates, entries, lines, or accounts is needed.
---

# Adding a validation rule

1. Pick the input type from `validation/.../api`: `ReportPeriodInput` (request) or `LedgerSnapshot` (ledger at startup).
   A new input type goes in `validation/src/main/java/com/campfire/validation/api/` as a plain record (no backend types).
2. Create `validation/src/main/java/com/campfire/validation/rules/<Name>Rule.java`:
   ```java
   public enum <Name>Rule implements ValidationRule<LedgerSnapshot> {
       INSTANCE;
       @Override public Class<LedgerSnapshot> target() { return LedgerSnapshot.class; }
       @Override public ValidationResult validate(LedgerSnapshot input) { ... return ValidationResult.of(errors); }
   }
   ```
   Stateless. Money comparisons with `compareTo`. Messages name the entry id and the amounts.
3. Add `<Name>Rule.INSTANCE` to `ValidationRules.all()` (`ValidationRulesTest` fails if you forget).
4. Test in `validation/src/test/.../<Name>RuleTest.java`: one passing case, one per failure mode.
5. `mvn -q verify`; restart the backend and check the startup log lists the new rule id.
6. If the rule is new for a new input type, the caller must invoke `validationRegistry.validate(newInput)`
   (backend controller or `LedgerDataLoader`).

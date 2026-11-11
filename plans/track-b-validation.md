# Track B — Validation (one module, `validation`)

Owner paths: `validation/**`, `backend/.../validation/**`, `backend/.../config/ValidationConfig.java`.
Read first: `spec.md` §7.

## Pattern (follow it)
- Rule = `public enum XRule implements ValidationRule<Input> { INSTANCE; ... }` (stateless singleton).
- Add `XRule.INSTANCE` to `ValidationRules.all()`. That list is the only registration.
- Error messages are plain sentences the API returns as-is, e.g. `"JE-007 is not balanced: debits 9000.00, credits 8999.00"`.

## Tasks (all done)
0. Collapse `validation-api` + `validation-rules` into one `validation` module; drop `plugins/`,
   ServiceLoader and `PluginConfig` (now `ValidationConfig`). — done
1. Request rules (`ReportPeriodInput`):
   - `DateRangePresentRule` — `"start is required"`, `"end is required"` (both if both missing).
   - `StartNotAfterEndRule` — done.
2. Ledger rules (`LedgerSnapshot`), each reports every offending entry id. Null amounts count as zero.
   - `EntryBalancedRule` — sum(debit) == sum(credit) per entry (`compareTo`, not `equals`, because of scale).
   - `LineOneSidedRule` — exactly one of debit/credit is non-zero on each line.
   - `NonNegativeAmountRule` — no negative debit/credit.
   - `KnownAccountRule` — every line's account is in `accountNumbers`.
   - `KnownStatusRule` — status ∈ {posted, draft, void}.
   - `MinimumTwoLinesRule` — an entry has at least 2 lines.

## Tests
- One test class per rule in `validation`: a passing case and each failing case, using small
  hand-built `LedgerSnapshot`s. Include a balanced entry with different scales (`"100.0"` vs `"100.00"`).
- `ValidationRulesTest` — every rule enum in the package is listed in `all()` (guards "forgot to register").
- `ValidationRegistryTest` (backend) — only rules whose `target()` matches run; errors aggregate.
- `RealLedgerRulesTest` (backend) — `data/ledger.json` passes all ledger rules.
- `RequestValidationTest` (backend, MockMvc) — missing dates → 400 with the rule messages.

## Done when
`mvn -q verify` green; `GET /income-statement?end=2026-03-31` → 400 `"start is required"`; startup log
lists all 8 rules.

## Status
Done: 8 rules registered, 33 rule tests + 8 backend tests, `mvn -q verify` green. `spring-boot:run` logs
`Loaded 8 validation rules`; missing/inverted dates return 400 with the rule messages.
Assumptions: null debit/credit count as zero; null status or account is reported as unknown (not an NPE).

## Cross-track requests
- Track A: `LedgerDataLoader` should build a `LedgerSnapshot` and fail startup on
  `validationRegistry.validate(snapshot)` errors (spec §4). Rules are ready; nothing else needed from B.

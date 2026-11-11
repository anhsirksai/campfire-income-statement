# Test scenarios

Expected values come from `spec.md` §6, computed independently from `data/ledger.json`.
Mutation check (2026-11-11): changing the query to include drafts and use an exclusive end date
made 7 service tests fail, so the tests do catch real accounting mistakes.

## Implemented

| Area | Scenario | Test |
| --- | --- | --- |
| Statement | Q1 2026 net income = -44480.14 | `IncomeStatementServiceTest.Q1_2026.netIncome` |
| Statement | Q1 section totals + gross profit / operating income | `Q1_2026.subtotals` |
| Statement | Q1 every account line, name and amount | `Q1_2026.everyLine` |
| Statement | Contra revenue (4900) negative, reduces Revenue | `Q1_2026.contraRevenueReducesRevenue` |
| Statement | Draft JE-019 excluded (Salaries 55500.00, not 60500.00) | `Q1_2026.draftEntryIsExcluded` |
| Statement | Void JE-009 excluded (2026-02-03 → 8200.00, not 16400.00) | `OtherRanges.voidEntryIsExcluded` |
| Statement | Vendor credit reduces Software (1099.97) | `Q1_2026.vendorCreditReducesExpense` |
| Statement | Inactive account 6300 with posted activity still shown | `Q1_2026.inactiveAccountWithPostedActivityIsReported` |
| Statement | Balance-sheet accounts never appear | `Q1_2026.balanceSheetAccountsAreNotOnTheStatement` |
| Statement | Sections in order REVENUE, COGS, OPERATING_EXPENSES, OTHER_INCOME | `Q1_2026.sectionsAreInStatementOrder` |
| Dates | Both ends inclusive (2026-03-31 single day → -17457.82) | `OtherRanges.bothEndsAreInclusive` |
| Dates | Range ending 03-31 excludes JE-024 on 04-01 | `OtherRanges.q1EndBoundaryExcludesApril1` |
| Dates | Dec 2025 → 5000.00; Apr 2026 → 9100.00 | `OtherRanges.december2025OnlyHasJe001`, `april2026OnlyHasJe024` |
| Dates | No activity → four empty sections, all 0.00 | `OtherRanges.rangeWithNoActivityHasFourEmptySections` |
| Contract | Q1 JSON shape, amounts are strings `^-?\d+\.\d{2}$` | `IncomeStatementControllerTest.q1ResponseMatchesContract` |
| Contract | Empty sections still present with "0.00" | `IncomeStatementControllerTest.emptySectionsAreStillPresentWithZeroTotals` |
| Errors | start after end → 400 | `IncomeStatementControllerTest.startAfterEndIsBadRequest` |
| Errors | Malformed date → 400 | `IncomeStatementControllerTest.malformedDateIsBadRequest` |
| Loader | Failing ledger rule stops startup before any insert | `LedgerDataLoaderTest.failingLedgerRuleStopsStartupBeforeAnyInsert` |
| Loader | Missing ledger file → LedgerDataException | `LedgerDataLoaderTest.missingFileIsALedgerDataException` |
| Loader | Real ledger parses 15 accounts / 25 entries, exact BigDecimal | `LedgerDataLoaderTest.realLedgerParsesWithExactAmounts` |
| Strategy | Debit-normal vs credit-normal sign per account type | `NormalBalanceStrategyFactoryTest` |
| Rules | Start == end valid; start > end fails | `StartNotAfterEndRuleTest` |

## Not yet implemented

| Area | Scenario | Owner |
| --- | --- | --- |
| Errors | Missing `start` and/or `end` → 400 "start is required" | Track B (`DateRangePresentRule`) |
| Loader | Real ledger passes every plugin ledger rule (balanced, one-sided, known account, known status, non-negative) | Track B |
| Rules | One test per ledger rule (pass + each failure mode) | Track B |
| Plugin | Rules loaded at runtime from the plugin jar, not the classpath | Track B |
| Frontend | Money formatting: grouping, parentheses for negatives, zero | Track C |
| Frontend | Table renders Q1 fixture: (44,480.14) net income, (800.25) for 4900 | Track C |
| Frontend | Client maps a 400 problem response to an error message | Track C |
| End to end | Verifier agent: run app, curl 5 golden ranges + error cases | After merge |
| Edge | Very large range (all time) equals sum of sub-ranges | Nice to have |

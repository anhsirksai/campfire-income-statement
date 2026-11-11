# Verification report: full app with 12 validation rules

Run on 11 Nov 2026 by the verifier agent against commit `a16cabb`: the validation fixes for missing fields, duplicate ids and the both-zero schema test, on top of `83f4d8c`. It covers the backend, the `validation` module and the frontend. The backend ran on port 8080 and the frontend on port 3001, because Docker held 3000. Golden values come from `spec.md` §6.

These commits were then rebased onto the frontend fix `cfd5362`. On the rebased tree, `mvn -q verify` passes, and `npm test` passes 18 tests with a clean `npm run build`.

**Result: 9 of 9 checks pass, with no blocking failures. Q1 2026 net income is (44,480.14), from the backend and through the frontend.**

## Checks

| Check | Result | Evidence |
| --- | --- | --- |
| Backend and validation tests | Pass | `mvn -q verify` exits 0. All 85 test cases pass, including the new rule tests, 6 `SchemaConstraintsTest` cases and the missing-amount loader test. |
| Frontend tests and build | Pass | `npm test` passes 17 tests in 3 files (18 after the rebase). `npm run build` exits 0. |
| Backend startup | Pass | The log shows `Loaded 12 validation rules`, then the ledger loading with 15 accounts, 25 journal entries and 51 lines. There are no errors or warnings. |
| Golden numbers | Pass | All 27 values across the 5 date ranges in spec §6 match as exact strings, including all 9 Q1 lines. |
| Error responses | Pass | All 5 bad requests return HTTP 400 with an `errors` array. |
| API contract | Pass | All 16 Q1 amounts are JSON strings matching `^-?\d+\.\d{2}$`. |
| Money hygiene, Java | Pass | No `double` or `float` appears in code. The grep matches 4 lines, all of them comments ("Double entry…", "double-posting", "never double"). |
| Money hygiene, frontend | Pass | Nothing calls `parseFloat` or `Number()` on an amount. The only grep match is the comment in `money.ts` that forbids them. |
| Frontend proxy | Pass | `/api/income-statement` for Q1 returns net income `-44480.14`, the same as the backend. A bad request returns the same 400. |

## Validation rules

The app loads 12 rules, and the real ledger passes all of them. To prove each ledger rule works, I broke one field of JE-002 (or account 4000) in a copy of `ledger.json` and started the app on that copy. Each run stopped at startup with only that rule's message. The commands are in `README.md` under "Validation rules (12)".

| Rule | Kind | Checks | Message when triggered |
| --- | --- | --- | --- |
| `DateRangePresentRule` | Request | `start` and `end` are present | `start is required` |
| `StartNotAfterEndRule` | Request | start is on or before end | `start must be on or before end` |
| `RequiredFieldsRule` | Ledger | amounts, ids, dates and account numbers are present | `JE-002 line 1 is missing a credit amount` |
| `UniqueIdsRule` | Ledger | entry ids and account numbers are unique | `journal entry JE-002 appears 2 times` |
| `EntryBalancedRule` | Ledger | debits equal credits per entry | `JE-002 is not balanced: debits 12450.75, credits 12450.00` |
| `LineOneSidedRule` | Ledger | exactly one of debit or credit per line | `JE-002 line 1 must have exactly one of debit or credit: debit 12450.75, credit 1.00` |
| `NonNegativeAmountRule` | Ledger | no negative amounts | `JE-002 line 1 has a negative amount: debit -12450.75, credit 0.00` |
| `AmountScaleRule` | Ledger | at most 2 decimal places | `JE-002 line 1 has more than 2 decimal places: debit 12450.755` |
| `KnownAccountRule` | Ledger | line accounts exist | `JE-002 line 2 uses unknown account 4999` |
| `AccountClassificationRule` | Ledger | account type and subtype agree | `account 4000 has type expense but subtype operating_revenue` |
| `KnownStatusRule` | Ledger | status is posted, draft or void | `JE-002 has unknown status 'pending'` |
| `MinimumTwoLinesRule` | Ledger | at least 2 lines per entry | `JE-002 has 0 lines; an entry needs at least 2` |

## Schema constraints (`schema.sql`, spec.md §4.1)

| Constraint | Result | Evidence |
| --- | --- | --- |
| `company.id UUID DEFAULT RANDOM_UUID()` | Pass | The insert lists only name and currency, so the default fills in the id. Every response still returns Northwind Coffee Roasters, USD. |
| Status must be posted, draft or void | Pass | All 25 entries are accepted. Draft JE-019 and void JE-009 are still stored and still excluded from the numbers. |
| Debit and credit are not negative | Pass | All 51 lines are accepted, and `rejectsNegativeAmount` passes. |
| Exactly one of debit or credit is set | Pass | All 51 lines are accepted. `rejectsLineWithBothSides` and `rejectsLineWithNeitherSide` pass. |
| Account type and subtype agree | Pass | All 15 accounts are accepted, and `rejectsAccountWhoseTypeAndSubtypeDisagree` passes. |
| Line position is unique within an entry | Pass | All 51 lines are accepted, and `rejectsDuplicateLinePosition` passes. |
| Index on `(status, entry_date)` | Pass | It is created without error at startup. A test can't show whether a query uses an index, and the results are unchanged. |

## Golden numbers

This run checked each value against the live API. An earlier run got the same figures from a separate Python `Decimal` calculation on `data/ledger.json`, counting posted entries only, with both dates inclusive. The ledger data has not changed since.

| Range (inclusive) | What it tests | Net income | API | Python |
| --- | --- | ---: | :---: | :---: |
| 2026-01-01 to 2026-03-31 | Q1, full statement | (44,480.14) | match | match |
| 2025-12-01 to 2025-12-31 | JE-001 only | 5,000.00 | match | match |
| 2026-04-01 to 2026-04-30 | JE-024 only | 9,100.00 | match | match |
| 2026-03-31 only | Both ends are inclusive | (17,457.82) | match | match |
| 2026-02-03 only | Void JE-009 is excluded | 4,920.00 | match | match |

## Q1 2026 statement as returned (USD)

| Account | Line | Source | Amount |
| --- | --- | --- | ---: |
| 4000 | Product Revenue | JE-002, 010, 016 | 35,650.75 |
| 4100 | Subscription Revenue | JE-005, 014, 021 | 3,000.00 |
| 4900 | Sales Returns & Discounts | JE-012, 016 | (800.25) |
| | **Total revenue** | | **37,850.50** |
| 5000 | Cost of Goods Sold | JE-003, 011, 017 | 14,272.75 |
| | **Gross profit** | | **23,577.75** |
| 6000 | Salaries | Draft JE-019 excluded | 55,500.00 |
| 6100 | Rent | JE-007: Jan–Mar rent paid in January, reported as recorded | 9,000.00 |
| 6200 | Software | JE-018 less vendor credit JE-020 | 1,099.97 |
| 6300 | Marketing (legacy) | JE-008: account inactive, still reported | 2,500.10 |
| | **Total operating expenses** | | **68,100.07** |
| | **Operating income** | | **(44,522.32)** |
| 7000 | Interest Income | JE-023 | 42.18 |
| | **Net income** | | **(44,480.14)** |

The API also returns the sections in the order the contract requires: REVENUE, COGS, OPERATING_EXPENSES, OTHER_INCOME.

## Bad requests

Every request below returns HTTP 400 as `application/problem+json`.

| Request | `errors` |
| --- | --- |
| `start=2026-03-31&end=2026-01-01` | `["start must be on or before end"]` |
| `start=2026-13-01&end=2026-12-31` | `["start must be a date in YYYY-MM-DD format"]` |
| `start=2026-01-01` | `["end is required"]` |
| `end=2026-03-31` | `["start is required"]` |
| no parameters | `["start is required", "end is required"]` |

## Notes

- A missing debit or credit used to pass validation, because the rules read it as 0, and then the app crashed on insert. `RequiredFieldsRule` now reports it, and `LedgerDataLoaderTest.missingAmountOrLinesIsReportedNotAnInsertCrash` checks that startup stops before any insert.
- I checked `rejectsLineWithNeitherSide` against a weakened constraint that rejects only lines with both sides set. That version made this test fail, and none of the earlier tests caught it.
- The 10 ledger rules run at startup, and the app refuses to start if any of them fails, so a clean start shows that `ledger.json` passed them all. No log line says so explicitly; one added line in `LedgerDataLoader` would make that visible.
- `IncomeStatementServiceTest` reports `tests="0"` at suite level because its 16 tests live in nested classes. All 16 ran and passed.

## How to reproduce

```bash
mvn -q verify
mvn -q install -DskipTests && mvn -q spring-boot:run
curl -s 'localhost:8080/income-statement?start=2026-01-01&end=2026-03-31' | jq .netIncome   # "-44480.14"
curl -s 'localhost:8080/income-statement?end=2026-03-31' | jq .errors                      # ["start is required"]
```

To trigger each ledger rule, see "Validation rules (12)" in `README.md`.

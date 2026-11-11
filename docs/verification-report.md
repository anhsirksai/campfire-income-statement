# Verification report: full app after schema changes

Run on 11 Nov 2026 against commit `d552fc1` ("Schema: justify differences from the data dictionary, add database guards"). It covers the backend, the `validation` module and the frontend. The backend ran on port 8081 and the frontend on port 3001, because 8080 and 3000 were in use; that doesn't change any result. Golden values come from `spec.md` §6.

**Result: 9 of 9 checks pass, with no blocking failures. Q1 2026 net income is (44,480.14), from the backend and through the frontend.**

## Checks

| Check | Result | Evidence |
| --- | --- | --- |
| Backend and validation tests | Pass | `mvn -q verify` exits 0. All 71 test cases pass, including the 4 new `SchemaConstraintsTest` cases. |
| Frontend tests and build | Pass | `npm test` passes 17 tests in 3 files. `npm run build` exits 0. |
| Backend startup | Pass | The schema applies with no errors or warnings. The log shows `Loaded 8 validation rules`, then the ledger loading with 15 accounts, 25 journal entries and 51 lines. |
| Golden numbers | Pass | All 27 values across the 5 date ranges in spec §6 match as exact strings, including all 9 Q1 lines. |
| Error responses | Pass | All 5 bad requests return HTTP 400 with an `errors` array. |
| API contract | Pass | All 51 amounts across the 5 responses are JSON strings matching `^-?\d+\.\d{2}$`. |
| Money hygiene, Java | Pass | No `double` or `float` appears in code. The grep matches 3 lines, all of them comments ("Double entry…", "never double"). |
| Money hygiene, frontend | Pass | Nothing calls `parseFloat` or `Number()` on an amount. The only grep match is the comment in `money.ts` that forbids them. |
| Frontend proxy | Pass | `/api/income-statement` for Q1 returns net income `-44480.14`, the same as the backend. A bad request returns the same 400. |

## Schema changes (`schema.sql`, spec.md §4.1)

| Change | Result | Evidence |
| --- | --- | --- |
| `company.id UUID DEFAULT RANDOM_UUID()` | Pass | The insert lists only name and currency, so the default fills in the id. Every response still returns Northwind Coffee Roasters, USD. |
| Status must be posted, draft or void | Pass | All 25 entries are accepted. Draft JE-019 and void JE-009 are still stored and still excluded from the numbers. |
| Debit and credit are not negative | Pass | All 51 lines are accepted, and `rejectsNegativeAmount` passes. |
| Exactly one of debit or credit is set | Pass | All 51 lines are accepted, and `rejectsLineWithBothSides` passes. |
| Line position is unique within an entry | Pass | All 51 lines are accepted, and `rejectsDuplicateLinePosition` passes. |
| Index on `(status, entry_date)` | Pass | It is created without error at startup. A test can't show whether a query uses an index, and the results are unchanged. |

## Golden numbers

This run checked each value against the live API. An earlier run, made before the schema change, got the same figures from a separate Python `Decimal` calculation on `data/ledger.json`. That calculation uses posted entries only, with both dates inclusive. The ledger data has not changed since.

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

- The 6 ledger rules run at startup, and the app refuses to start if any of them fails, so a clean start shows that `ledger.json` passed them all. No log line says so explicitly. One added line in `LedgerDataLoader` would make that visible.
- `SchemaConstraintsTest` has no case for a line where debit and credit are both 0. The one-sided check should reject that line too, and one more test would prove it.
- `IncomeStatementServiceTest` reports `tests="0"` at suite level because its 16 tests live in nested classes. All 16 ran and passed.

## How to reproduce

```bash
mvn -q verify
mvn -q install -DskipTests && mvn -q spring-boot:run
curl -s 'localhost:8080/income-statement?start=2026-01-01&end=2026-03-31' | jq .netIncome   # "-44480.14"
curl -s 'localhost:8080/income-statement?end=2026-03-31' | jq .errors                      # ["start is required"]
```

# Verification report: backend and validation

Run on 11 Nov 2026 against the Spring Boot backend on port 8080. Golden values come from `spec.md` §6.

**Result: 7 of 7 applicable checks pass, with no blocking failures. Q1 2026 net income is (44,480.14).**

The backend, `validation`, `data/` and `api/` code verified is identical to this commit. The frontend was not part of this run.

## Checks

| Check | Result | Evidence |
| --- | --- | --- |
| Build and tests | Pass | `mvn -q verify` exits 0. There are 67 test cases across backend and validation, with no failures or errors. |
| Backend startup | Pass | Ready in about 5 s. The log shows `Loaded 8 validation rules`, then the ledger loading with 15 accounts, 25 journal entries and 51 lines. There are no warnings or errors. |
| Golden numbers | Pass | All 5 date ranges in spec §6 match as exact strings, including all 9 Q1 lines. |
| Error responses | Pass | All 5 bad requests return HTTP 400 with an `errors` array. |
| API contract | Pass | All 16 amount fields in the Q1 response are JSON strings matching `^-?\d+\.\d{2}$`. |
| Money hygiene | Pass | No `double` or `float` appears in code. The grep matches 3 lines, all of them comments ("Double entry…", "never double"). |
| Cleanup | Pass | The server processes were stopped, port 8080 is free, and the working tree is clean. |
| Frontend | Skipped | Not included in this run. |

## Golden numbers

Each value was checked against the live API and against a separate Python `Decimal` calculation from `data/ledger.json`. That calculation uses posted entries only, with both dates inclusive.

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
- `IncomeStatementServiceTest` reports `tests="0"` at suite level because its 16 tests live in nested classes. All 16 ran and passed. That is why the suite totals add up to 51, while the full count is 67.

## How to reproduce

```bash
mvn -q verify
mvn -q install -DskipTests && mvn -q spring-boot:run
curl -s 'localhost:8080/income-statement?start=2026-01-01&end=2026-03-31' | jq .netIncome   # "-44480.14"
curl -s 'localhost:8080/income-statement?end=2026-03-31' | jq .errors                      # ["start is required"]
```

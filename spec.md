# Spec (Stage 2: Design)

## 1. Architecture
```
Browser ──> Next.js (frontend/, :3000) ──/api/* rewrite──> Spring Boot (backend/, :8080) ──JDBC──> H2 (in-memory)
                                                                  │
                                                      validation (plain Java jar, normal dependency)
```
Maven modules: `validation` (rule interface, inputs, rules; no Spring, no deps) ← `backend`.

## 2. API contract
Source of truth: `api/openapi.yaml`. Summary:

`GET /income-statement?start=2026-01-01&end=2026-03-31` →
```json
{
  "company": "Northwind Coffee Roasters", "currency": "USD",
  "start": "2026-01-01", "end": "2026-03-31",
  "sections": [
    {"key": "REVENUE", "title": "Revenue", "total": "37850.50", "lines": [
      {"accountNumber": "4000", "accountName": "Product Revenue", "amount": "35650.75"},
      {"accountNumber": "4100", "accountName": "Subscription Revenue", "amount": "3000.00"},
      {"accountNumber": "4900", "accountName": "Sales Returns & Discounts", "amount": "-800.25"}]},
    {"key": "COGS", "title": "Cost of goods sold", "total": "14272.75", "lines": ["..."]},
    {"key": "OPERATING_EXPENSES", "title": "Operating expenses", "total": "68100.07", "lines": ["..."]},
    {"key": "OTHER_INCOME", "title": "Other income", "total": "42.18", "lines": ["..."]}
  ],
  "grossProfit": "23577.75", "operatingIncome": "-44522.32", "netIncome": "-44480.14"
}
```
- Amounts: decimal strings, always 2 decimals. Section `total` and line `amount` are in the
  section's natural sign (revenue positive, expenses positive). Contra revenue is negative.
- All four sections are always present, even when empty (`lines: []`, `total: "0.00"`).
- Errors: 400 `application/problem+json` (Spring `ProblemDetail`) with `detail` and `errors[]`.
- Backend DTOs (`model/dto`) are hand-written records that mirror the YAML. Frontend types are
  generated from the YAML (`npm run gen:api` → `src/lib/api/schema.d.ts`).

## 3. Backend layers (`backend/src/main/java/com/campfire/ledger/`)
| Package | Responsibility | Depends on |
| --- | --- | --- |
| `controller` | Bind params, run request validation, call service, return DTO | service, validation, dto |
| `service` | Build the statement: group balances into sections, subtotals | repository, strategy, model |
| `strategy` | `NormalBalanceStrategy` (debit-normal / credit-normal) + factory by `AccountType` | model |
| `repository` | `LedgerRepository`: domain queries (company, posted balances in range) | dao |
| `dao` (+`mapper`) | One DAO per table, `NamedParameterJdbcTemplate`, RowMappers, inserts for the loader | query, model |
| `query` | SQL text constants (`LedgerQueries`) | nothing |
| `model` (+`dto`) | Records/enums: `Account`, `AccountBalance`, `Company`, enums; API DTOs | nothing |
| `validation` | `ValidationRegistry` — runs the rules whose `target()` matches the input | validation |
| `config` | `ValidationConfig` (registry bean), `LedgerDataLoader` (ledger.json → H2 at startup), web config | all |
| `exception` | `InvalidRequestException`, `LedgerDataException`, `GlobalExceptionHandler` | — |

Logging: `logback-spring.xml`, console + rolling `logs/ledger.log`. INFO: request params, rows loaded,
rules loaded. Set `org.springframework.jdbc.core` to DEBUG to see SQL.

## 4. Data
`schema.sql`: `company`, `accounts`, `journal_entries` (`entry_date`, `status`), `journal_lines`
(`entry_id`, `line_no`, `account_number`, `debit DECIMAL(19,2)`, `credit DECIMAL(19,2)`).
Enum-like columns store the JSON's lowercase values.

### 4.1 Schema vs the data dictionary
| Difference | Why |
| --- | --- |
| `lines[]` → own table `journal_lines` (FK `entry_id`) | One-to-many. The statement is "sum lines per account, posted, in range": one JOIN + GROUP BY. The general ledger *is* the set of lines. Drill-down, balance sheet, "include drafts" become WHERE/GROUP BY changes. |
| `line_no` added | SQL rows have no order; keeps the entry's lines as recorded (audit trail). `UNIQUE (entry_id, line_no)`. |
| `journal_lines.id` surrogate key | Lines have no id in the data; a stable handle for future drill-down links. |
| `date` → `entry_date` | `DATE` is a reserved word/type; the name also says it is the accounting date. |
| `lines[].account` → `account_number` | It is the account's number, an FK to `accounts.number`. |
| `debit`/`credit` → `DECIMAL(19,2)` | Dictionary says decimal strings: exact storage, no float drift. |
| `date` → `DATE` | "No time or time zone". |
| `number`, `id` → `VARCHAR` | Strings in the data; identifiers, not quantities (keeps leading zeros). |
| `type`/`subtype`/`status` → lowercase `VARCHAR` | Stored values match the source; Java enums map them with `fromCode`. |
| New `company` table, `id UUID` PK | `company`/`currency` are root-level JSON fields the response needs. UUID key so a second entity can be added later. |
| Draft and void entries stored | Void "stays on record for the audit trail"; filtering is in the query. |

**Constraints as a second guard.** The validation rules (spec §7) check the ledger before any insert. The
database repeats the row-level ones so a direct insert cannot bypass them: FKs on `entry_id` and
`account_number`, `status IN ('posted','draft','void')`, `debit >= 0 AND credit >= 0`, exactly one of
debit/credit non-zero, and type/subtype pairs that agree (`chk_account_classification`). "Entry is balanced"
spans rows, so it stays a rule only (`EntryBalancedRule`). Amount scale is also rule-only (`AmountScaleRule`):
`DECIMAL(19,2)` would silently round `0.005` rather than reject it.

**Locale.** Enum codes convert with `Locale.ROOT`, so lowercase JSON codes map the same way on any machine
(a Turkish default locale would otherwise uppercase `i` to `İ`).

**Indexes.** `idx_journal_entries_status_date (status, entry_date)`: equality column first, then the date
range, matching the statement's WHERE. No separate `journal_lines(entry_id)` index: H2 indexes every FK, and
`UNIQUE (entry_id, line_no)` already leads with `entry_id`. At 25 entries indexes are not measurable; they
document the access path. Next if the ledger grows: index `journal_lines(account_number)` for per-account
drill-down (needed explicitly on PostgreSQL, which does not auto-index FKs).

`LedgerDataLoader` (ApplicationRunner): read `app.ledger-file` with Jackson (amounts as `BigDecimal`),
build a `LedgerSnapshot`, run ledger rules through `ValidationRegistry`; any error → `LedgerDataException`
(app refuses to start). Then insert company, accounts, entries, lines via DAOs. All entries are stored
(including draft/void) so the audit trail is intact; filtering happens in the query.

Core query (in `LedgerQueries`):
```sql
SELECT a.number, a.name, a.type, a.subtype, a.is_active,
       SUM(l.debit) AS total_debit, SUM(l.credit) AS total_credit
FROM journal_lines l
JOIN journal_entries e ON e.id = l.entry_id
JOIN accounts a        ON a.number = l.account_number
WHERE e.status = 'posted' AND e.entry_date BETWEEN :start AND :end
GROUP BY a.number, a.name, a.type, a.subtype, a.is_active
ORDER BY a.number
```

## 5. Computation (service)
1. `balances = repository.findPostedBalances(start, end)`.
2. For each balance: `section = StatementSection.fromSubtype(subtype)`; skip if empty (balance sheet).
   `amount = NormalBalanceStrategyFactory.forType(type).balance(debit, credit)`.
   - revenue / other_income (type revenue, credit-normal): credit − debit → positive.
   - contra_revenue (type revenue): credit − debit → **negative**, reduces Revenue total.
   - cogs / operating_expense (type expense, debit-normal): debit − credit → positive; vendor credit reduces it.
3. Skip lines whose amount is zero? **No** — show any account that had posted activity (even if it nets
   to 0.00), so the accountant can see it moved.
4. Section total = sum of its lines. `grossProfit = REVENUE − COGS`; `operatingIncome = grossProfit − OPERATING_EXPENSES`;
   `netIncome = operatingIncome + OTHER_INCOME`. Subtotals are written out explicitly, not derived from a loop.
5. All values `setScale(2)` (inputs already have scale 2; no rounding is expected to occur).

## 6. Decisions and golden numbers
| # | Decision | Why |
| --- | --- | --- |
| D1 | Only `posted`; `draft` (JE-019) and `void` (JE-009, JE-025) excluded | Not in the books |
| D2 | `start` and `end` inclusive | "Between two dates" in accounting means both ends |
| D3 | Inactive account 6300 is still reported | Inactive blocks *new* postings; history stays |
| D4 | Contra revenue is a negative line inside Revenue (no separate "net revenue" row) | Matches required sections |
| D5 | Report as recorded: JE-007 rent for Jan–Mar stays in January | Assignment says do not re-accrue |
| D6 | Interest income (type revenue, subtype other_income) goes to Other income | Subtype decides section |
| D7 | Amounts are JSON strings | Exactness end to end |
| D8 | Missing/invalid/inverted dates → 400 | Fail loudly, not empty statement |
| D9 | Unbalanced/malformed ledger data → app fails at startup | Wrong books must not produce numbers |

Golden: **Q1 2026 (2026-01-01..2026-03-31)**
| Line | Amount |
| --- | --- |
| 4000 Product Revenue (JE-002, 010, 016) | 35,650.75 |
| 4100 Subscription Revenue (JE-005, 014, 021) | 3,000.00 |
| 4900 Sales Returns & Discounts (JE-012, 016) | (800.25) |
| **Revenue** | **37,850.50** |
| 5000 COGS (JE-003, 011, 017) | 14,272.75 |
| **Gross profit** | **23,577.75** |
| 6000 Salaries (JE-006, 015, 022; JE-019 draft excluded) | 55,500.00 |
| 6100 Rent (JE-007) | 9,000.00 |
| 6200 Software (JE-018 − JE-020) | 1,099.97 |
| 6300 Marketing (legacy) (JE-008) | 2,500.10 |
| **Operating expenses** | **68,100.07** |
| **Operating income** | **(44,522.32)** |
| 7000 Interest Income (JE-023) | 42.18 |
| **Net income** | **(44,480.14)** |

Other ranges: Dec 2025 → NI 5,000.00 (JE-001 only). April 2026 → NI 9,100.00 (JE-024).
2026-03-31..2026-03-31 → Revenue 1,000.00, OpEx 18,500.00, Other 42.18, NI (17,457.82).
2026-02-03 only → JE-009 void excluded: Revenue 8,200.00, COGS 3,280.00, NI 4,920.00.

## 7. Validation
- One module/jar, `validation`. Package `api`: `ValidationRule<T>` (`target()`, `validate()`, `id()`),
  `ValidationResult`, inputs `ReportPeriodInput`, `LedgerSnapshot` (plain records, no backend types).
  Package `rules`: the rules and `ValidationRules.all()`, the single list of rules the app runs.
- Rules are **enum singletons** (`INSTANCE`), stateless.
- `ValidationConfig` (backend) builds `ValidationRegistry` from `ValidationRules.all()`; the registry runs
  the rules whose `target()` matches the input.
- Why not a runtime-loaded plugin jar (the first design): URLClassLoader + ServiceLoader added concepts the
  hand-extension round does not need. A normal dependency keeps "new rule = enum + one line" with nothing to deploy.
- Request rules: `DateRangePresentRule`, `StartNotAfterEndRule`. Ledger rules: `RequiredFieldsRule`
  (account number, entry id, date, debit and credit present; the other rules read a missing amount as 0, so
  without it a bad ledger would pass and crash on insert), `UniqueIdsRule` (entry ids and account numbers),
  `EntryBalancedRule`,
  `LineOneSidedRule`, `NonNegativeAmountRule`, `AmountScaleRule` (at most 2 decimals), `KnownAccountRule`,
  `AccountClassificationRule` (type and subtype agree: the section comes from subtype, the sign from type),
  `KnownStatusRule`, `MinimumTwoLinesRule`.
- `LedgerSnapshot` carries `accounts` (number, type, subtype as raw codes) and `entries`; `accountNumbers()` is derived.

## 8. Frontend (`frontend/src/`)
- `app/page.tsx`: client component, holds `start`/`end` state (default 2026-01-01..2026-03-31), fetches on submit.
- `components/DateRangeForm.tsx`, `components/IncomeStatementTable.tsx` (sections, lines, subtotals rows).
- `lib/api/client.ts`: `getIncomeStatement(start, end)` → typed result or `{error}` from ProblemDetail.
- `lib/api/schema.d.ts`: generated, never edited by hand.
- `lib/format/money.ts`: `formatMoney("-44480.14") → "(44,480.14)"`, pure string ops.
- `next.config.ts` rewrite `/api/:path*` → `http://localhost:8080/:path*` (no CORS needed).
- Tests: Vitest + Testing Library for `money.ts` and the table (Q1 fixture renders net income).

## 9. Extension points (for the pairing round)
- New section/subtype → `AccountSubtype` + `StatementSection.fromSubtype` + subtotal line in the service.
- Include drafts / status filter → one query param, one `WHERE` clause in `LedgerQueries`.
- Comparative periods → call the service twice; add `periods[]` to the contract.
- New validation → new enum rule + one line in `ValidationRules.all()`.
- Balance sheet → new query (`entry_date <= :asOf`), new service; strategies already cover all types.

# Track A — Backend data layer + statement

Owner paths: `backend/**` except `backend/.../validation/**` and `config/ValidationConfig.java`.
Read first: `spec.md` §3–§6. Do not change `api/openapi.yaml` or DTO shapes.

## Tasks
1. **query/LedgerQueries.java** — SQL constants: inserts for company/accounts/entries/lines,
   `SELECT_COMPANY`, `SELECT_POSTED_BALANCES` (spec §4, named params `:start`, `:end`, status literal `'posted'`
   or param `:status` bound from `EntryStatus.POSTED.code()`).
2. **dao/** — `CompanyDao`, `AccountDao`, `JournalEntryDao`, `JournalLineDao` with `NamedParameterJdbcTemplate`.
   Insert methods for the loader; `JournalLineDao.sumPostedByAccount(start, end)` returns `List<AccountBalance>`.
   **dao/mapper/** — `AccountBalanceRowMapper` (uses `rs.getBigDecimal`, never `getDouble`), `CompanyRowMapper`.
3. **repository/JdbcLedgerRepository.java** implements `LedgerRepository` over the DAOs.
4. **config/LedgerDataLoader.java** — `ApplicationRunner`. Read `app.ledger-file` with Jackson into
   small private JSON records (`BigDecimal` amounts, `USE_BIG_DECIMAL_FOR_FLOATS` on), map to
   `LedgerSnapshot`, call `validationRegistry.validate(snapshot)`; errors → `LedgerDataException`.
   Insert everything in one `@Transactional` method. Log counts at INFO.
5. **service/IncomeStatementServiceImpl.java** — replace the stub per spec §5. Always emit all four
   sections in enum order. Explicit subtotal lines.
6. **config/WebConfig.java** — optional CORS for `http://localhost:3000` (frontend uses a rewrite, so low priority).

## Tests (write first where cheap)
- `IncomeStatementServiceTest` (Spring Boot test against real H2 + ledger.json):
  - Q1 golden: every line and every subtotal from spec §6 (assert with `isEqualByComparingTo` or exact string).
  - Dec 2025 → NI 5000.00; April → 9100.00; 2026-03-31 single day → NI -17457.82; 2026-02-03 → NI 4920.00 (void excluded).
  - JE-019 draft excluded: Q1 salaries = 55500.00 (not 60500.00).
  - Contra revenue negative: 4900 line = -800.25 and Revenue total = 37850.50.
  - Vendor credit: 6200 = 1099.97.
  - Empty range (e.g. 2025-01-01..2025-01-31) → four empty sections, all totals 0.00.
- `NormalBalanceStrategyFactoryTest` — each AccountType → right sign.
- `IncomeStatementControllerTest` — add: Q1 JSON shape matches contract (`$.sections.length()==4`,
  keys in order, `$.netIncome == "-44480.14"` as a **string**), missing `end` → 400 (after Track B adds `DateRangePresentRule`).
- `LedgerDataLoaderTest` — unbalanced entry fixture → `LedgerDataException` (after merge with B; can use a fake rule until then).

## Done when
`mvn -q verify` green; `curl` Q1 returns `"netIncome":"-44480.14"`; no `double` in the codebase
(`grep -rn "double\|Double\|float" backend/src/main` is empty).

## Cross-track requests
(none yet)

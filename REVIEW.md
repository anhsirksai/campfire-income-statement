# REVIEW.md — rules for automated and human PR review

Review every track merge and PR against these passes, in order. Report findings as
`[SEVERITY] file:line — problem → fix`. Stop at the first BLOCKER pass if there are many.

## Severity scale and caps
| Severity | Meaning | Merge? |
| --- | --- | --- |
| BLOCKER | Wrong numbers, inexact money, contract drift, failing build/tests | No |
| MAJOR | Missing test for an accounting rule, layering violation, unhandled bad input | Fix before merge unless time is out; then log in NOTES.md |
| MINOR | Naming, readability, dead code, logging gaps | Optional |
| NIT | Style | Do not report unless asked |

Cap: at most 10 findings per review. Never report NITs alongside BLOCKERs.

## Pass 1 — Correctness (accounting)
- Only `posted` entries reach the numbers (check the SQL `WHERE`, not just the service).
- Range is inclusive both ends (`BETWEEN` or `>=`/`<=`), no time-zone conversion of `LocalDate`.
- Signs: revenue/other income = credit − debit; expenses = debit − credit; contra revenue negative inside Revenue.
- Subtotals: GP = Revenue − COGS; OI = GP − OpEx; NI = OI + Other income.
- Golden Q1 net income `-44480.14` still asserted and passing.

## Pass 2 — Money compliance
- No `double`, `float`, `Double`, `Float` in backend money paths; no `rs.getDouble`.
- `BigDecimal` compared with `compareTo`/`isEqualByComparingTo`, not `equals`, unless scale is fixed.
- JSON amounts are strings. Frontend never `parseFloat`/`Number()`/`+x` on an amount.

## Pass 3 — Contract compliance
- Response matches `api/openapi.yaml` (field names, required fields, section order, string amounts).
- `frontend/src/lib/api/schema.d.ts` regenerated if the YAML changed; DTO records updated to match.
- Contract changes only on main, never inside a track branch.

## Pass 4 — Architecture
- controller → service → repository → dao → query. No SQL outside `query/`. No `JdbcTemplate` outside `dao/`.
- Validation rules live in the `validation` module, not in `backend`.
- New rules are enum singletons and are listed in `ValidationRules.all()`.
- Track ownership respected (see CLAUDE.md).

## Pass 5 — Tests
- Each accounting rule has a test that would fail if the rule were broken (not just a happy path).
- No test asserts only "status 200" or "not null".
- Tests use the real `data/ledger.json` for golden numbers, small fixtures for edge cases.

## Pass 6 — Readability (for hand extension)
- Could someone extend this in 28 minutes without AI? Flag indirection that does not pay for itself.
- Names say what accounting concept they are. Comments explain *why* (accounting reason), not *what*.
- Log lines at INFO for request params and load counts; no logging of every row.

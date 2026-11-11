# Plan (master)

Budget: 2 hours total. Correct numbers with a plain page beat a pretty page with wrong numbers.

## Status
| Stage | State |
| --- | --- |
| Track 0 — scaffold (docs, contract, data, Maven modules, H2 schema, stubs, Next.js app) | done |
| Track A — backend data + statement | todo → `plans/track-a-backend.md` |
| Track B — validation | todo → `plans/track-b-validation.md` |
| Track C — frontend | todo → `plans/track-c-frontend.md` |
| Merge + verify + NOTES/README/video | todo |

## What Track 0 left in place
- `api/openapi.yaml` (frozen), `data/ledger.json`, `schema.sql`, `application.yml`, `logback-spring.xml`.
- Model enums/records, DTO records, strategy + factory (done, tested indirectly by Track A).
- `LedgerRepository` interface, `IncomeStatementService` + stub impl (throws → 501).
- `IncomeStatementController` (complete), `GlobalExceptionHandler` (complete).
- `ValidationRegistry` (complete), `PluginConfig` (now `ValidationConfig`; plugin loading dropped, see spec §7).
- One example rule `StartNotAfterEndRule` + `LedgerRulesProvider` (now `ValidationRules`).
- `frontend/` = untouched `create-next-app` output (Next 16, TS, App Router, src dir).
- `mvn -q verify` is green (5 tests).

## Parallel run
```bash
claude --worktree backend      # Track A (or run it in this main checkout)
claude --worktree validation   # Track B
claude --worktree frontend     # Track C
```
Each session: "Read CLAUDE.md, spec.md, and plans/track-X.md, then execute the track." Tracks are
independent because they meet only at `api/openapi.yaml` (frozen) and the rule interface in `validation`
(owned by B; A only *calls* `ValidationRegistry.validate`).

Suggested time box (wall clock, parallel): A 45 min, B 30 min, C 40 min. Then 20 min merge/verify,
15 min NOTES/README.

## Merge sequence (in main)
1. Merge B. `mvn -q verify`.
2. Merge A. `mvn -q verify`. Run backend, curl Q1 → `-44480.14`.
3. Merge C. `cd frontend && npm test && npm run build`. Open :3000, Q1 shows (44,480.14).
4. Run the `verifier` agent. Fix anything it reports.
5. Fill `NOTES.md` (Q1 answer, decisions D1–D9 from spec §6, how verified, AI notes, next steps). Finalize `README.md`.

## Definition of done
- Golden Q1 + the 4 other ranges in spec §6 are asserted in backend tests.
- Draft, void, out-of-range, boundary dates, contra revenue, vendor credit each have a test that fails
  if the rule is broken.
- Bad requests → 400 with message; ledger integrity rules run at startup.
- Frontend shows all sections and subtotals, negatives in parentheses, handles 400.
- README commands work from a clean clone.

## Cut list if short on time (in order)
1. Frontend component tests (keep money formatter test).
2. `KnownStatusRule`, `NonNegativeAmountRule`.
Never cut: golden-number tests, draft/void exclusion, exact money.

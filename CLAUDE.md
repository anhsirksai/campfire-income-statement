# CLAUDE.md

Income statement (P&L) take-home: Spring Boot + JDBC + H2 backend, Next.js frontend, one endpoint
`GET /income-statement?start=YYYY-MM-DD&end=YYYY-MM-DD`. Assignment: `docs/assignment/README.md`.

Read in this order before changing anything: `intent.md` → `spec.md` → `plan.md` → your track file in `plans/`.

## Commands (run from repo root unless noted)
| What | Command |
| --- | --- |
| Build + all backend tests | `mvn -q verify` |
| One module's tests | `mvn -q -pl backend -am test` (or `-pl validation`) |
| Run backend (port 8080) | `mvn -q spring-boot:run` from the repo root (run `mvn -q install -DskipTests` first so the `validation` jar is installed) |
| Smoke check | `curl -s 'localhost:8080/income-statement?start=2026-01-01&end=2026-03-31'` |
| Frontend dev (port 3000) | `cd frontend && npm run dev` |
| Frontend tests / build | `cd frontend && npm test && npm run build` |
| Regenerate API types | `cd frontend && npm run gen:api` |

Toolchain: JDK 26 (compiles with `release 21`), Maven 3.10, Spring Boot 3.5.7, Node 24, npm 12, Next 16.

## Non-negotiable rules
- **Money is exact.** Java: `BigDecimal` only, scale 2. SQL: `DECIMAL(19,2)`. JSON: decimal *strings*.
  TypeScript: never `parseFloat`/`Number()` an amount; format the string. No `double`/`float` anywhere near money.
- **Only `posted` entries count.** `draft` and `void` never affect numbers.
- **Date range is inclusive** on both ends. Dates are plain `LocalDate`, no time zones.
- **The contract is `api/openapi.yaml`.** Do not change response shape in code first. Contract changes
  happen on `main` only, then both sides follow (see `.claude/skills/contract-change`).
- **Layering (backend):** controller → service → repository → dao → query. Never skip a layer.
  SQL strings live only in `query/`. Services never see SQL or HTTP types.
- **Golden numbers:** Q1 2026 net income is **-44480.14**. Any change that moves it is a bug unless
  `spec.md` is updated with the reason. Full breakdown in `spec.md` §6.
- Keep it small and readable. The code is extended **by hand, without AI** in the follow-up interview.
  Prefer plain, explicit code over clever abstraction. No new framework without a reason in `spec.md`.

## Parallel worktree ownership (avoid merge conflicts)
| Track | Worktree | Owns |
| --- | --- | --- |
| A backend | `claude --worktree backend` | `backend/**` except the B paths below |
| B validation | `claude --worktree validation` | `validation/**`, `backend/.../validation/**`, `backend/.../config/ValidationConfig.java` |
| C frontend | `claude --worktree frontend` | `frontend/**` |
| main | this checkout | `api/openapi.yaml`, root docs, `data/ledger.json`, merges |

Touching another track's files: stop and note it in your track file under "Cross-track requests" instead.
Merge order: B → A → C. After each merge run `mvn -q verify`.

## Workflow per change
1. Check the change against `intent.md` / `spec.md`. If it changes a decision, update `spec.md` first.
2. Write the test that would catch the accounting mistake, then the code.
3. `mvn -q verify` (and frontend tests if touched). Run the `verifier` agent before merging a track.
4. Small commits with real messages, **no `Co-Authored-By` trailer**. Record data assumptions in `NOTES.md` as you go.

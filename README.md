# Income statement

A small full-stack app that shows an income statement (P&L) for Northwind Coffee Roasters for any
date range. Assignment: [`docs/assignment/README.md`](docs/assignment/README.md). Decisions and the
Q1 2026 answer: [`NOTES.md`](NOTES.md).

## Versions
| Tool | Version |
| --- | --- |
| JDK | 26 (code compiled with `--release 21`; any JDK 21+ works) |
| Maven | 3.10 |
| Spring Boot | 3.5.7 |
| H2 | in-memory, bundled |
| Node / npm | 24 / 12 |
| Next.js / React | 16 / 19 |

## Run
```bash
# 1. Build everything once
mvn -q install -DskipTests

# 2. Backend on :8080
mvn -q spring-boot:run              # from the repo root; first run downloads dependencies
curl 'localhost:8080/income-statement?start=2026-01-01&end=2026-03-31'

# 3. Frontend on :3000 (new terminal)
cd frontend && npm install && npm run dev
open http://localhost:3000
```
H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:ledger`, user `sa`, no password).

## Test
```bash
mvn -q verify                       # backend + validation rules
cd frontend && npm test             # frontend
```

## Layout
```
api/openapi.yaml      API contract shared by backend and frontend
data/ledger.json      chart of accounts + journal entries (loaded into H2 at startup)
validation/           validation rules (request dates, ledger data); plain Java, used by backend
backend/              Spring Boot: controller → service → repository → dao → query
frontend/             Next.js page
intent.md, spec.md, plan.md, plans/   how the work was planned and split
```

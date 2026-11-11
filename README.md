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

## Validation rules (12)
All 12 rules live in `validation/` and are listed in `ValidationRules.all()`. On startup the app logs
`Loaded 12 validation rules: [...]`.

### Request rules: a bad request returns 400
Start the backend (see Run), then:

- **DateRangePresentRule:** `start` and `end` are both required.
  `curl -s 'localhost:8080/income-statement?end=2026-03-31'` → `["start is required"]`
- **StartNotAfterEndRule:** `start` must be on or before `end`. A single day (start = end) is valid.
  `curl -s 'localhost:8080/income-statement?start=2026-04-01&end=2026-03-31'` → `["start must be on or before end"]`

### Ledger rules: bad data stops the app at startup
Each demo copies the ledger, breaks one thing with `jq`, and starts the app on the copy. The real
`data/ledger.json` is never touched. Build once with `mvn -q install -DskipTests`. `--server.port=0` avoids a
clash with a backend that is already running.

```bash
demo() { jq "$1" data/ledger.json > /tmp/bad.json && \
  java -jar backend/target/backend-0.1.0.jar --app.ledger-file=/tmp/bad.json --server.port=0 2>&1 \
  | grep -o 'Ledger data failed validation: .*'; }
```

JE-002 is January product sales: line 1 debits 1100 for 12450.75, and line 2 credits 4000 for 12450.75.

- **RequiredFieldsRule:** every debit, credit, entry id, date and account number is present. A missing amount is an error, never read as 0.
  `demo 'del(.journal_entries[1].lines[0].credit)'` → `JE-002 line 1 is missing a credit amount`
- **UniqueIdsRule:** entry ids and account numbers are unique.
  `demo '.journal_entries[2].id = "JE-002"'` → `journal entry JE-002 appears 2 times`
- **EntryBalancedRule:** each entry's debits equal its credits (`100.0` equals `100.00`).
  `demo '.journal_entries[1].lines[1].credit = "12450.00"'` → `JE-002 is not balanced: debits 12450.75, credits 12450.00`
- **LineOneSidedRule:** each line has exactly one of debit or credit.
  `demo '.journal_entries[1].lines[0].credit = "1.00" | .journal_entries[1].lines[1].debit = "1.00"'` → `JE-002 line 1 must have exactly one of debit or credit: debit 12450.75, credit 1.00` (and line 2)
- **NonNegativeAmountRule:** no negative amounts. The direction comes from debit vs credit, not from a minus sign.
  `demo '.journal_entries[1].lines[0].debit = "-12450.75" | .journal_entries[1].lines[1].credit = "-12450.75"'` → `JE-002 line 1 has a negative amount: debit -12450.75, credit 0.00` (and line 2)
- **AmountScaleRule:** at most 2 decimal places. The database would round `0.005` without any error.
  `demo '.journal_entries[1].lines[0].debit = "12450.755" | .journal_entries[1].lines[1].credit = "12450.755"'` → `JE-002 line 1 has more than 2 decimal places: debit 12450.755` (and line 2)
- **KnownAccountRule:** every line's account exists in the chart of accounts.
  `demo '.journal_entries[1].lines[1].account = "4999"'` → `JE-002 line 2 uses unknown account 4999`
- **AccountClassificationRule:** an account's type and subtype agree. The section comes from the subtype and the sign from the type.
  `demo '.accounts[6].type = "expense"'` → `account 4000 has type expense but subtype operating_revenue`
- **KnownStatusRule:** status is `posted`, `draft` or `void`.
  `demo '.journal_entries[1].status = "pending"'` → `JE-002 has unknown status 'pending'`
- **MinimumTwoLinesRule:** an entry has at least 2 lines.
  `demo 'del(.journal_entries[1].lines)'` → `JE-002 has 0 lines; an entry needs at least 2`

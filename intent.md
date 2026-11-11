# Intent (Stage 1: Plan)

## Problem
An accountant at Northwind Coffee Roasters wants to see the income statement (P&L) for any date
range, built from the chart of accounts and journal entries in `data/ledger.json`.

## Users
- **Accountant (primary reader).** Wants standard section order, one line per account, subtotals,
  negatives in parentheses, and numbers that tie out to the journal to the cent.
- **Reviewing engineers.** Want to read the code path from dates to numbers in a few minutes, and
  extend it by hand (no AI) in a 28-minute pairing session.

## Goals
1. Pick a start date and end date, see the statement for that inclusive range.
2. Sections in this order, each with a line per account and a subtotal:
   Revenue → Cost of goods sold → **Gross profit** → Operating expenses → **Operating income** →
   Other income → **Net income**.
3. Numbers are correct for *any* range, not only Q1 2026.
4. Amounts are exact. No floating-point drift.
5. Tests catch real accounting mistakes (wrong sign, draft/void counted, off-by-one dates, contra
   revenue added instead of subtracted), not just typos.
6. Assumptions about the data are noticed, decided, and written down in `NOTES.md`.

## Non-goals
Styling, auth, deployment, Docker, balance sheet, multi-currency, editing entries, performance at scale.

## Success criteria
- `GET /income-statement?start=2026-01-01&end=2026-03-31` returns net income `-44480.14`.
- Bad input (missing date, malformed date, start after end) returns 400 with a clear message.
- README lets a reviewer run backend, frontend, and tests on a Mac in a few minutes.
- A new requirement on the same ledger (likely: comparative periods, include drafts toggle, account
  roll-ups, balance sheet, new validation) can be added by touching one or two obvious places.

## Constraints
2-hour budget. Java 21+/Spring Boot, JDBC (not JPA), H2 in memory, Next.js + React, OpenAPI contract
shared by both sides, validation rules in their own module (one plain Java jar the backend depends on).

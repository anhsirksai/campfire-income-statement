# Track C — Frontend (Next.js 16, App Router, TypeScript)

Owner paths: `frontend/**`. Read first: `spec.md` §2 and §8, `frontend/CLAUDE.md`, `frontend/AGENTS.md`.
Next 16 differs from older versions: check `frontend/node_modules/next/dist/docs/` before using an API.
The backend may not be ready; build against the contract and a fixture.

## Tasks
1. **Clean the create-next-app output**: remove demo `page.module.css` content, public SVGs, boilerplate
   README; minimal `globals.css` (system font, tabular numbers: `font-variant-numeric: tabular-nums`).
2. **next.config.ts**: add `rewrites()` `/api/:path*` → `${process.env.BACKEND_URL ?? "http://localhost:8080"}/:path*`.
3. **Contract types**: `npm i -D openapi-typescript`; script `"gen:api": "openapi-typescript ../api/openapi.yaml -o src/lib/api/schema.d.ts"`;
   commit the generated file. Export friendly aliases (`IncomeStatement`, `Section`, `Line`, `Problem`) from `lib/api/types.ts`.
4. **lib/api/client.ts**: `getIncomeStatement(start, end): Promise<{ok: true, data} | {ok: false, errors: string[]}>`.
   Reads ProblemDetail `errors[]`/`detail` on non-2xx; network failure → friendly message.
5. **lib/format/money.ts**: `formatMoney(amount: string): string` — string-only: validate `/^-?\d+\.\d{2}$/`,
   add thousands separators, negatives in parentheses (`"-800.25"` → `"(800.25)"`), `"0.00"` → `"0.00"`. No `Number()`.
6. **components/DateRangeForm.tsx** — two `<input type="date">` + submit; client-side check start ≤ end (server is still the authority).
7. **components/IncomeStatementTable.tsx** — semantic `<table>`: header (company, period, currency);
   for each section a heading row, one row per line (`4000 · Product Revenue`), a "Total {title}" row;
   computed rows **Gross profit** after COGS, **Operating income** after OpEx, **Net income** last (bold, double rule).
   Amounts right-aligned. Empty section shows "No activity".
8. **app/page.tsx** — `"use client"`; state start/end default `2026-01-01`/`2026-03-31`; load on mount and on submit; loading and error states.

## Tests (Vitest + @testing-library/react + jsdom)
- `money.test.ts`: grouping (`"1234567.89"` → `"1,234,567.89"`), negative, zero, small (`"42.18"`), rejects malformed.
- `IncomeStatementTable.test.tsx`: render a Q1 fixture (`src/test/fixtures/q1.json`, copy of spec §2/§6 values) →
  shows `(44,480.14)` in the Net income row, `(800.25)` for 4900, sections in order.
- `client.test.ts`: 400 ProblemDetail → `{ok:false, errors:["start must be on or before end"]}` (mock `fetch`).

## Done when
`npm test`, `npm run lint`, `npm run build` pass; with backend running, `npm run dev` shows the Q1 statement and a 400 message for start > end.

## Cross-track requests
(none yet)

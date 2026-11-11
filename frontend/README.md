# Frontend

Next.js 16 (App Router) page that shows the income statement for a date range.

```bash
npm install
npm run dev        # http://localhost:3000 (use -p 3100 if 3000 is taken)
npm test           # Vitest + Testing Library
npm run lint
npm run build
npm run gen:api    # regenerate src/lib/api/schema.d.ts from ../api/openapi.yaml
```

The browser calls `/api/income-statement`; `next.config.ts` rewrites `/api/*` to the backend
(`BACKEND_URL`, default `http://localhost:8080`). Start the backend first.

No backend yet? Run the mock, which always returns the Q1 2026 fixture (and a 400 for start > end):

```bash
MOCK_PORT=8090 npm run mock
BACKEND_URL=http://localhost:8090 npm run dev -- -p 3100
```

| Path | What |
| --- | --- |
| `src/app/page.tsx` | Page state (dates, statement, errors); loads Q1 2026 on mount |
| `src/components/DateRangeForm.tsx` | Start/end inputs, client-side start ≤ end check |
| `src/components/IncomeStatementTable.tsx` | Sections, lines, section totals, gross profit / operating income / net income |
| `src/lib/api/client.ts` | The only place that calls `fetch`; turns ProblemDetail into `errors[]` |
| `src/lib/api/schema.d.ts` | Generated from the contract, never edited by hand |
| `src/lib/format/money.ts` | String-only money formatting: `"-44480.14"` → `"(44,480.14)"` |
| `src/test/fixtures/q1.json` | Q1 2026 response from spec §2/§6, used by tests |

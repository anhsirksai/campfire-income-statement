@AGENTS.md

# frontend/CLAUDE.md

Next.js 16 (App Router, `src/` dir, TypeScript), React 19. Plain CSS, no UI library.

- API types come only from `src/lib/api/schema.d.ts`, generated from `../api/openapi.yaml` (`npm run gen:api`). Never hand-edit it.
- All HTTP goes through `src/lib/api/client.ts`. Components never call `fetch`.
- The browser calls `/api/...`; `next.config.ts` rewrites it to the backend (`BACKEND_URL`, default `http://localhost:8080`).
- Amounts are strings. Format with `src/lib/format/money.ts`. Never `parseFloat`, `Number()`, or `+amount`.
- Keep components small and presentational; state lives in `app/page.tsx`.
- Tests: Vitest + Testing Library, next to the code (`*.test.ts(x)`).

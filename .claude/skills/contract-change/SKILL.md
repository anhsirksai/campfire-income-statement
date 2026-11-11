---
name: contract-change
description: Change the API contract (api/openapi.yaml) and propagate it to backend DTOs and frontend types. Use whenever a request/response field, param, or error shape changes.
---

# Changing the API contract

The contract is `api/openapi.yaml`. Code follows the contract, never the other way round.

1. Confirm you are on `main` (not a track worktree). If not, write the request under
   "Cross-track requests" in your track file and stop.
2. Edit `api/openapi.yaml`. Keep amounts as `$ref: Money` (decimal strings). Mark new fields `required`
   unless there is a real reason for optional.
3. Update `spec.md` §2 (example JSON) and, if a decision changed, §6.
4. Backend: update the records in `backend/src/main/java/com/campfire/ledger/model/dto/`.
   Money fields are `BigDecimal` with `@JsonFormat(shape = JsonFormat.Shape.STRING)`.
5. Frontend: `cd frontend && npm run gen:api`, then fix TypeScript errors (`npx tsc --noEmit`).
6. Update the contract assertions in `IncomeStatementControllerTest` and the frontend fixture `src/test/fixtures/q1.json`.
7. `mvn -q verify && (cd frontend && npm test -- --run)`. Commit as one change: "Contract: <what changed>".

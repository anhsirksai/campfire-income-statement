# TODO: record a journal entry (POST from UI and backend)

Goal: an accountant enters a journal entry in the UI; the backend validates it with the same rules the ledger
loader uses, stores it, and the income statement reflects it immediately (if posted and in range).

Order of work: contract → validation → backend → frontend → tests → docs. Contract first (CLAUDE.md rule).

## 0. Decisions to make first (write the answers into spec.md §6)
- [ ] **Which statuses can be created?** Proposed: `posted` and `draft` only; `void` is a later action on an existing entry.
- [ ] **Who assigns the id?** Proposed: server assigns the next `JE-NNN` (client never sends `id`). Alternative: UUID.
- [ ] **Inactive accounts.** The data dictionary says `is_active` = "whether new entries can be posted today".
  So a new entry must **reject** lines on inactive accounts (e.g. 6300), even though historical lines stay reported.
- [ ] **Back-dated entries** (date in a past period): allowed for now; no locked periods exist. Note it.
- [ ] **Immutability.** Posted entries are not edited or deleted (audit trail); corrections are new entries. No PUT/DELETE.
- [ ] **Persistence.** H2 is in-memory and reloads `ledger.json` on restart, so created entries are lost on restart.
  Accept for the take-home and say so in the UI/README, or switch to a file-backed H2 URL.

## 1. Contract (`api/openapi.yaml`, on main, via `.claude/skills/contract-change`)
- [ ] `POST /journal-entries`
  - Request `NewJournalEntry`: `date` (date), `status` (`posted|draft`), `memo` (string, optional),
    `lines[]` of `{account, debit: Money, credit: Money}` (at least 2).
  - `201 Created` + `Location: /journal-entries/{id}` + body `JournalEntry` (with the assigned `id`).
  - `400` problem+json with `errors[]` (one message per failed rule) — same shape as today.
- [ ] `GET /accounts` → `[{number, name, type, subtype, isActive}]` so the UI can offer an account picker
  and grey out inactive accounts.
- [ ] Regenerate frontend types (`npm run gen:api`); add backend DTO records in `model/dto`.

## 2. Validation (`validation/`)
Reuse the existing ledger rules; add only what is new for a single incoming entry.
- [ ] New input type `NewEntryInput(LedgerSnapshot.Entry entry, List<LedgerSnapshot.Account> accounts)` in `api/`,
  or build a one-entry `LedgerSnapshot` and reuse the rules as-is (preferred: no rule changes).
- [ ] Rules that must fire (already exist): `EntryBalancedRule`, `LineOneSidedRule`, `NonNegativeAmountRule`,
  `AmountScaleRule`, `KnownAccountRule`, `KnownStatusRule`, `MinimumTwoLinesRule`.
- [ ] New `ActiveAccountRule` (target: new-entry input only, **not** the startup snapshot, since history may use
  inactive accounts): every line's account has `is_active = true`. Needs `isActive` on `LedgerSnapshot.Account`.
- [ ] New `CreatableStatusRule`: status is `posted` or `draft`.
- [ ] Required-field rule for the new input: `date` present, every line has an account and amounts.
- [ ] Register new rules in `ValidationRules.all()`; one small test each.

## 3. Backend (`backend/`, same layering: controller → service → repository → dao → query)
- [ ] `query/LedgerQueries`: `SELECT_ACCOUNTS`, `SELECT_MAX_ENTRY_ID` (or a sequence for `JE-NNN`),
  `SELECT_ENTRY_BY_ID` + its lines.
- [ ] `dao/AccountDao.findAll()` (+ `AccountRowMapper`); `JournalEntryDao.nextId()`, `findById()`.
  Inserts already exist (`JournalEntryDao.insert`, `JournalLineDao.insert`) — reuse them.
- [ ] `repository/LedgerRepository`: `findAccounts()`, `save(JournalEntry)` (entry + lines in one transaction),
  `findEntry(id)`. Move the insert loop out of `LedgerDataLoader` into `save` so loader and POST share it.
- [ ] `service/JournalEntryService.create(NewJournalEntry)`: map DTO → snapshot input, run
  `validationRegistry.validate(...)`, throw `InvalidRequestException(errors)` on failure, assign id, `save`, return.
  `@Transactional`.
- [ ] `controller/JournalEntryController`: `POST /journal-entries` → 201 + Location; `GET /accounts`.
- [ ] `GlobalExceptionHandler`: map `DataIntegrityViolationException` (DB CHECK/FK as the second guard) → 400
  with a generic message, so a rule gap never becomes a 500.
- [ ] `WebConfig` CORS: allow `POST` (only matters when calling the API directly, not via the /api rewrite).
- [ ] Log at INFO: created id, date, status, line count.

## 4. Frontend (`frontend/src/`)
- [ ] `lib/api/client.ts`: `getAccounts()`, `createJournalEntry(entry)` → `{ok, data} | {ok:false, errors[]}`
  (same pattern as `getIncomeStatement`).
- [ ] `components/JournalEntryForm.tsx`: date, status (posted/draft), memo, dynamic line rows
  (account select from `GET /accounts`, inactive accounts disabled, debit, credit), add/remove line.
- [ ] Live totals: debits vs credits with a "balanced / out by X" hint. Do the sum on decimal **strings**
  (add a `sumMoney` helper in `lib/format/money.ts` using integer cents), never `parseFloat`.
- [ ] Client-side checks are hints only; always show the server's `errors[]` from a 400.
- [ ] On 201: clear the form, show the new id, and refetch the income statement so the change is visible.
- [ ] Place the form on `app/page.tsx` below the statement (or a `/journal-entries/new` route).

## 5. Tests (minimal)
- [ ] Controller: valid posted entry → 201, then Q1 net income changes by exactly the entry's effect.
- [ ] Controller: unbalanced → 400 with "is not balanced"; inactive account 6300 → 400; draft → 201 but
  statement unchanged.
- [ ] One test per new rule.
- [ ] Frontend: form shows server errors from a 400; `sumMoney` adds `"0.10" + "0.20"` = `"0.30"`.

## 6. Docs
- [ ] `spec.md`: decisions from §0, new endpoints, new rules; `docs/test-scenarios.md`: new scenarios.
- [ ] `README.md`: curl example for POST; note that created entries are in-memory only.
- [ ] Note: golden numbers in tests assume the shipped ledger only; POST tests must use their own Spring
  context or roll back (`@Transactional` on the test) so they don't shift other tests' totals.

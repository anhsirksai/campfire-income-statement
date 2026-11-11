# Notes

## Net income, Q1 2026 (2026-01-01 to 2026-03-31)
**TODO after merge** — expected from hand calculation: **(44,480.14)**. Confirm the app shows it.

## Decisions and assumptions about the data
<!-- Summarize spec.md §6 D1–D9 in one line each, plus anything found during the build. -->
The validation rules and apis have come out seperate repo. I merged them to make it less complicated for now.

## How I checked the numbers
<!-- Independent Python/Decimal computation from ledger.json (.claude/skills/check-numbers), golden tests for 5 ranges, verifier agent. -->

Mock data showed how it should appear in the UI and also verified it against the ACCOUNTING_PREMIER. overall negative balance shows in () and in red. not using -. 


## Where AI helped / where it was wrong
<!-- Example already seen: proposed a static provider() method for ServiceLoader singletons; that only works on the module path, not the classpath. Switched to a provider class + enum singletons. -->

AI helped to the larger extent of taking my design. 
overall with the claude --workflow, I am able to have three agents build UI and mock it to show me how I can integrate with backend while it still building.
The other agent built validations and I was able to direct it in the way I want implementation to proceed for this take home.
For the backend, I was able to first comeup with complete plan, verify and only then move to implementation. after compiling the scafolding.

can attach three seperate jsonl files to have better evaluation.

where it complicated things: validation repo.
writing too many unit test which is consuming resources and valuable time while I want to validate the implementation in the two hour mark.


## Next with more time
<!-- -->

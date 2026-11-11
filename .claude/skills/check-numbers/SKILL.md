---
name: check-numbers
description: Hand-check income statement numbers for any date range directly from data/ledger.json, independent of the app. Use to confirm a golden value, write a new test expectation, or debug a mismatch.
---

# Independent number check

Never trust the app to check itself. Compute from the raw JSON with exact decimals:

```bash
python3 - 2026-01-01 2026-03-31 <<'PY'
import json, sys
from decimal import Decimal as D
start, end = sys.argv[1], sys.argv[2]
L = json.load(open("data/ledger.json"))
acct = {a["number"]: a for a in L["accounts"]}
bal = {}
for e in L["journal_entries"]:
    if e["status"] != "posted" or not (start <= e["date"] <= end):
        continue
    for l in e["lines"]:
        a = acct[l["account"]]
        if a["subtype"] == "balance_sheet":
            continue
        sign = 1 if a["type"] == "revenue" else -1          # credit-normal vs debit-normal
        bal[a["number"]] = bal.get(a["number"], D("0")) + sign * (D(l["credit"]) - D(l["debit"]))
sec = lambda *subs: sum((v for k, v in bal.items() if acct[k]["subtype"] in subs), D("0.00"))
rev, cogs, opex, oth = sec("operating_revenue", "contra_revenue"), sec("cogs"), sec("operating_expense"), sec("other_income")
for k in sorted(bal): print(k, acct[k]["name"], bal[k])
print("revenue", rev, "cogs", cogs, "gross", rev - cogs, "opex", opex, "operating", rev - cogs - opex, "other", oth, "net", rev - cogs - opex + oth)
PY
```
Compare with `curl -s 'localhost:8080/income-statement?start=...&end=...'`. If they differ, list the
entries in range (id, status, lines) and find which one the app treats differently.

import { render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import type { IncomeStatement } from "@/lib/api/types";
import q1 from "@/test/fixtures/q1.json";
import { IncomeStatementTable } from "./IncomeStatementTable";

const statement = q1 as IncomeStatement;

function rowFor(label: string): HTMLElement {
  const row = screen.getByText(label).closest("tr");
  if (!row) throw new Error(`No row for ${label}`);
  return row;
}

describe("IncomeStatementTable", () => {
  it("shows Q1 2026 net income as (44,480.14)", () => {
    render(<IncomeStatementTable statement={statement} />);
    expect(within(rowFor("Net income")).getByText("(44,480.14)")).toBeInTheDocument();
  });

  it("shows the subtotals", () => {
    render(<IncomeStatementTable statement={statement} />);
    expect(within(rowFor("Gross profit")).getByText("23,577.75")).toBeInTheDocument();
    expect(within(rowFor("Operating income")).getByText("(44,522.32)")).toBeInTheDocument();
  });

  it("shows contra revenue 4900 as a negative line", () => {
    render(<IncomeStatementTable statement={statement} />);
    expect(within(rowFor("4900 · Sales Returns & Discounts")).getByText("(800.25)")).toBeInTheDocument();
  });

  it("renders sections and subtotals in statement order", () => {
    const expected = [
      "Revenue",
      "Cost of goods sold",
      "Gross profit",
      "Operating expenses",
      "Operating income",
      "Other income",
      "Net income",
    ];
    render(<IncomeStatementTable statement={statement} />);
    const rowHeadings = screen.getAllByRole("row").map((row) => row.querySelector("th")?.textContent ?? "");
    expect(rowHeadings.filter((text) => expected.includes(text))).toEqual(expected);
  });

  it("shows 'No activity' for an empty section", () => {
    const empty: IncomeStatement = {
      ...statement,
      sections: statement.sections.map((s) => (s.key === "OTHER_INCOME" ? { ...s, lines: [], total: "0.00" } : s)),
    };
    render(<IncomeStatementTable statement={empty} />);
    expect(screen.getByText("No activity")).toBeInTheDocument();
  });
});

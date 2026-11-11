import { Fragment } from "react";
import type { IncomeStatement, Section, SectionKey } from "@/lib/api/types";
import { formatMoney, isNegative } from "@/lib/format/money";

type Props = {
  statement: IncomeStatement;
};

type Subtotal = { label: string; amount: string; final?: boolean };

/** The subtotal row that follows each section, if any. Written out to match the service's formulas. */
function subtotalAfter(key: SectionKey, statement: IncomeStatement): Subtotal | null {
  switch (key) {
    case "COGS":
      return { label: "Gross profit", amount: statement.grossProfit };
    case "OPERATING_EXPENSES":
      return { label: "Operating income", amount: statement.operatingIncome };
    case "OTHER_INCOME":
      return { label: "Net income", amount: statement.netIncome, final: true };
    default:
      return null;
  }
}

function Amount({ value }: { value: string }) {
  return <td className={isNegative(value) ? "amount negative" : "amount"}>{formatMoney(value)}</td>;
}

function SectionRows({ section }: { section: Section }) {
  return (
    <>
      <tr className="section-heading">
        <th scope="rowgroup" colSpan={2}>
          {section.title}
        </th>
      </tr>
      {section.lines.length === 0 ? (
        <tr className="line empty">
          <td>No activity</td>
          <Amount value={section.total} />
        </tr>
      ) : (
        section.lines.map((line) => (
          <tr className="line" key={line.accountNumber}>
            <td>
              {line.accountNumber} · {line.accountName}
            </td>
            <Amount value={line.amount} />
          </tr>
        ))
      )}
      <tr className="section-total">
        <th scope="row">Total {section.title.toLowerCase()}</th>
        <Amount value={section.total} />
      </tr>
    </>
  );
}

export function IncomeStatementTable({ statement }: Props) {
  return (
    <table className="statement">
      <caption>
        <span className="company">{statement.company}</span>
        <span className="title">Income statement</span>
        <span className="period">
          {statement.start} to {statement.end} · {statement.currency}
        </span>
      </caption>
      <thead>
        <tr>
          <th scope="col">Account</th>
          <th scope="col" className="amount">
            Amount
          </th>
        </tr>
      </thead>
      <tbody>
        {statement.sections.map((section) => {
          const subtotal = subtotalAfter(section.key, statement);
          return (
            <Fragment key={section.key}>
              <SectionRows section={section} />
              {subtotal && (
                <tr className={subtotal.final ? "subtotal net-income" : "subtotal"}>
                  <th scope="row">{subtotal.label}</th>
                  <Amount value={subtotal.amount} />
                </tr>
              )}
            </Fragment>
          );
        })}
      </tbody>
    </table>
  );
}

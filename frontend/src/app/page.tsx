"use client";

import { useEffect, useState } from "react";
import { DateRangeForm } from "@/components/DateRangeForm";
import { IncomeStatementTable } from "@/components/IncomeStatementTable";
import { getIncomeStatement, type IncomeStatementResult } from "@/lib/api/client";
import type { IncomeStatement } from "@/lib/api/types";

const DEFAULT_START = "2026-01-01";
const DEFAULT_END = "2026-03-31";

export default function Home() {
  const [statement, setStatement] = useState<IncomeStatement | null>(null);
  const [errors, setErrors] = useState<string[]>([]);
  // Starts true: the default period is fetched on mount.
  const [loading, setLoading] = useState(true);

  function showResult(result: IncomeStatementResult) {
    if (result.ok) {
      setStatement(result.data);
      setErrors([]);
    } else {
      setStatement(null);
      setErrors(result.errors);
    }
    setLoading(false);
  }

  useEffect(() => {
    let cancelled = false;
    getIncomeStatement(DEFAULT_START, DEFAULT_END).then((result) => {
      if (!cancelled) showResult(result);
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function handleSubmit(start: string, end: string) {
    setLoading(true);
    showResult(await getIncomeStatement(start, end));
  }

  return (
    <main className="page">
      <h1>Income statement</h1>
      <DateRangeForm
        initialStart={DEFAULT_START}
        initialEnd={DEFAULT_END}
        loading={loading}
        onSubmit={handleSubmit}
      />

      {errors.length > 0 && (
        <div className="error-box" role="alert">
          <ul>
            {errors.map((message) => (
              <li key={message}>{message}</li>
            ))}
          </ul>
        </div>
      )}

      {loading && !statement && errors.length === 0 && <p className="status">Loading…</p>}
      {statement && (
        <div className={loading ? "stale" : undefined}>
          <IncomeStatementTable statement={statement} />
        </div>
      )}
    </main>
  );
}

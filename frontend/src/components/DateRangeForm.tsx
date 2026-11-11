"use client";

import { useState, type FormEvent } from "react";

type Props = {
  initialStart: string;
  initialEnd: string;
  loading: boolean;
  onSubmit: (start: string, end: string) => void;
};

/** Two date inputs. Checks start <= end before submitting; the server still validates. */
export function DateRangeForm({ initialStart, initialEnd, loading, onSubmit }: Props) {
  const [start, setStart] = useState(initialStart);
  const [end, setEnd] = useState(initialEnd);
  const [error, setError] = useState<string | null>(null);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!start || !end) {
      setError("Choose both a start and an end date.");
      return;
    }
    // YYYY-MM-DD strings compare correctly as text.
    if (start > end) {
      setError("Start date must be on or before end date.");
      return;
    }
    setError(null);
    onSubmit(start, end);
  }

  return (
    <form className="range-form" onSubmit={handleSubmit} noValidate>
      <label>
        Start
        <input type="date" name="start" value={start} onChange={(e) => setStart(e.target.value)} />
      </label>
      <label>
        End
        <input type="date" name="end" value={end} onChange={(e) => setEnd(e.target.value)} />
      </label>
      <button type="submit" disabled={loading}>
        {loading ? "Loading…" : "Show statement"}
      </button>
      {error && (
        <p className="form-error" role="alert">
          {error}
        </p>
      )}
    </form>
  );
}

import type { IncomeStatement, Problem } from "./types";

export type IncomeStatementResult =
  | { ok: true; data: IncomeStatement }
  | { ok: false; errors: string[] };

const NETWORK_ERROR = "Could not reach the server. Is the backend running?";

/** Fetch the income statement for an inclusive date range (YYYY-MM-DD). */
export async function getIncomeStatement(start: string, end: string): Promise<IncomeStatementResult> {
  const query = new URLSearchParams({ start, end });
  let response: Response;
  try {
    response = await fetch(`/api/income-statement?${query}`, {
      headers: { Accept: "application/json" },
    });
  } catch {
    return { ok: false, errors: [NETWORK_ERROR] };
  }

  if (response.ok) {
    const data = (await response.json()) as IncomeStatement;
    return { ok: true, data };
  }
  return { ok: false, errors: await readProblemErrors(response) };
}

/** Turn a ProblemDetail body into messages: errors[] first, then detail, then the HTTP status. */
async function readProblemErrors(response: Response): Promise<string[]> {
  let problem: Partial<Problem> | undefined;
  try {
    problem = (await response.json()) as Partial<Problem>;
  } catch {
    problem = undefined;
  }
  if (problem?.errors && problem.errors.length > 0) {
    return problem.errors;
  }
  if (problem?.detail) {
    return [problem.detail];
  }
  return [`Request failed (HTTP ${response.status}).`];
}

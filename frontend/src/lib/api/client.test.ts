import { afterEach, describe, expect, it, vi } from "vitest";
import q1 from "@/test/fixtures/q1.json";
import { getIncomeStatement } from "./client";

function mockFetch(impl: () => Promise<Response>) {
  const fn = vi.fn<typeof fetch>(impl);
  vi.stubGlobal("fetch", fn);
  return fn;
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("getIncomeStatement", () => {
  it("calls the /api rewrite with both dates and returns the statement", async () => {
    const fetchMock = mockFetch(async () => Response.json(q1));
    const result = await getIncomeStatement("2026-01-01", "2026-03-31");
    expect(fetchMock.mock.calls[0][0]).toBe("/api/income-statement?start=2026-01-01&end=2026-03-31");
    expect(result).toEqual({ ok: true, data: q1 });
  });

  it("returns ProblemDetail errors[] on 400", async () => {
    mockFetch(async () =>
      new Response(
        JSON.stringify({
          type: "about:blank",
          title: "Bad Request",
          status: 400,
          detail: "Invalid request",
          errors: ["start must be on or before end"],
        }),
        { status: 400, headers: { "Content-Type": "application/problem+json" } },
      ),
    );
    const result = await getIncomeStatement("2026-03-31", "2026-01-01");
    expect(result).toEqual({ ok: false, errors: ["start must be on or before end"] });
  });

  it("falls back to detail when errors[] is missing", async () => {
    mockFetch(async () => new Response(JSON.stringify({ title: "Bad Request", status: 400, detail: "end is required" }), { status: 400 }));
    expect(await getIncomeStatement("2026-01-01", "")).toEqual({ ok: false, errors: ["end is required"] });
  });

  it("reports the status when the body is not JSON", async () => {
    mockFetch(async () => new Response("Internal Server Error", { status: 500 }));
    expect(await getIncomeStatement("2026-01-01", "2026-03-31")).toEqual({
      ok: false,
      errors: ["Request failed (HTTP 500)."],
    });
  });

  it("returns a friendly message on network failure", async () => {
    mockFetch(async () => {
      throw new TypeError("Failed to fetch");
    });
    const result = await getIncomeStatement("2026-01-01", "2026-03-31");
    expect(result.ok).toBe(false);
    if (!result.ok) expect(result.errors[0]).toMatch(/could not reach the server/i);
  });
});

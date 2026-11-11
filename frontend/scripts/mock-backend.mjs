// Stand-in for the Spring Boot backend while it is not ready. Development only.
// Valid range → the Q1 2026 fixture (whatever dates you ask for). start > end or a missing date → ProblemDetail 400.
// Usage: npm run mock (port 8080, or MOCK_PORT=...)
import { readFileSync } from "node:fs";
import { createServer } from "node:http";

const port = Number(process.env.MOCK_PORT ?? 8080);
const q1 = readFileSync(new URL("../src/test/fixtures/q1.json", import.meta.url), "utf8");

function problem(res, errors) {
  res.writeHead(400, { "Content-Type": "application/problem+json" });
  res.end(JSON.stringify({ type: "about:blank", title: "Bad Request", status: 400, detail: "Invalid request", errors }));
}

createServer((req, res) => {
  const url = new URL(req.url, "http://localhost");
  console.log(`${req.method} ${url.pathname}${url.search}`);
  if (url.pathname !== "/income-statement") {
    res.writeHead(404).end();
    return;
  }
  const start = url.searchParams.get("start");
  const end = url.searchParams.get("end");
  if (!start || !end) {
    problem(res, ["start and end are required"]);
    return;
  }
  if (start > end) {
    problem(res, ["start must be on or before end"]);
    return;
  }
  res.writeHead(200, { "Content-Type": "application/json" }).end(q1);
}).listen(port, () => console.log(`Mock backend on http://localhost:${port} (always returns the Q1 2026 fixture)`));

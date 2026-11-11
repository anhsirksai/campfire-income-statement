import { describe, expect, it } from "vitest";
import { formatMoney, isNegative } from "./money";

describe("formatMoney", () => {
  it("adds thousands separators", () => {
    expect(formatMoney("1234567.89")).toBe("1,234,567.89");
    expect(formatMoney("37850.50")).toBe("37,850.50");
    expect(formatMoney("1000.00")).toBe("1,000.00");
  });

  it("leaves small amounts alone", () => {
    expect(formatMoney("42.18")).toBe("42.18");
    expect(formatMoney("999.99")).toBe("999.99");
  });

  it("puts negatives in parentheses", () => {
    expect(formatMoney("-800.25")).toBe("(800.25)");
    expect(formatMoney("-44480.14")).toBe("(44,480.14)");
  });

  it("shows zero without a sign", () => {
    expect(formatMoney("0.00")).toBe("0.00");
    expect(formatMoney("-0.00")).toBe("0.00");
  });

  it("keeps every digit of large amounts exactly (no float rounding)", () => {
    expect(formatMoney("90071992547409.93")).toBe("90,071,992,547,409.93");
  });

  it("rejects malformed amounts", () => {
    for (const bad of ["", "12", "12.3", "12.345", "1,000.00", "abc", "+5.00", "1e3", " 1.00"]) {
      expect(() => formatMoney(bad)).toThrow();
    }
  });
});

describe("isNegative", () => {
  it("is true only below zero", () => {
    expect(isNegative("-0.01")).toBe(true);
    expect(isNegative("-0.00")).toBe(false);
    expect(isNegative("0.00")).toBe(false);
    expect(isNegative("5.00")).toBe(false);
  });
});

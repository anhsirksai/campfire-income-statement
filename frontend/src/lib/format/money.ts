// Amounts arrive as exact decimal strings ("-44480.14"). Format them with string operations only:
// never Number(), parseFloat() or unary +, which would round through binary floating point.

const MONEY_PATTERN = /^-?\d+\.\d{2}$/;

/** "-44480.14" → "(44,480.14)", "1234567.89" → "1,234,567.89", "0.00" → "0.00". */
export function formatMoney(amount: string): string {
  if (!MONEY_PATTERN.test(amount)) {
    throw new Error(`Not a money string: "${amount}"`);
  }
  const negative = amount.startsWith("-");
  const unsigned = negative ? amount.slice(1) : amount;
  const [whole, cents] = unsigned.split(".");
  const grouped = whole.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  const formatted = `${grouped}.${cents}`;
  return isNegative(amount) ? `(${formatted})` : formatted;
}

/** True when the amount is below zero. "-0.00" is zero, not negative. */
export function isNegative(amount: string): boolean {
  return amount.startsWith("-") && /[1-9]/.test(amount);
}

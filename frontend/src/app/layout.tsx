import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Income Statement",
  description: "Income statement (P&L) for a date range",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

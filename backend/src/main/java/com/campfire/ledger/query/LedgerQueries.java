package com.campfire.ledger.query;

/** All SQL in the backend. Column names match schema.sql; parameters are named (:start, :end, ...). */
public final class LedgerQueries {

    private LedgerQueries() {
    }

    // ---- company ----

    public static final String INSERT_COMPANY = """
            INSERT INTO company (name, currency) VALUES (:name, :currency)
            """;

    public static final String SELECT_COMPANY = """
            SELECT name, currency FROM company
            """;

    // ---- accounts ----

    public static final String INSERT_ACCOUNT = """
            INSERT INTO accounts (number, name, type, subtype, is_active)
            VALUES (:number, :name, :type, :subtype, :isActive)
            """;

    // ---- journal entries and lines ----

    public static final String INSERT_JOURNAL_ENTRY = """
            INSERT INTO journal_entries (id, entry_date, status, memo)
            VALUES (:id, :entryDate, :status, :memo)
            """;

    public static final String INSERT_JOURNAL_LINE = """
            INSERT INTO journal_lines (entry_id, line_no, account_number, debit, credit)
            VALUES (:entryId, :lineNo, :accountNumber, :debit, :credit)
            """;

    /**
     * Debit and credit totals per account for entries with the given status and
     * :start <= entry_date <= :end (inclusive on both ends). Accounts with no lines in range are absent.
     */
    public static final String SUM_LINES_BY_ACCOUNT = """
            SELECT a.number, a.name, a.type, a.subtype, a.is_active,
                   SUM(l.debit)  AS total_debit,
                   SUM(l.credit) AS total_credit
            FROM journal_lines l
            JOIN journal_entries e ON e.id = l.entry_id
            JOIN accounts a        ON a.number = l.account_number
            WHERE e.status = :status
              AND e.entry_date BETWEEN :start AND :end
            GROUP BY a.number, a.name, a.type, a.subtype, a.is_active
            ORDER BY a.number
            """;
}

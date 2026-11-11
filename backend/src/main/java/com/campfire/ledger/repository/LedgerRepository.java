package com.campfire.ledger.repository;

import com.campfire.ledger.model.AccountBalance;
import com.campfire.ledger.model.Company;
import java.time.LocalDate;
import java.util.List;

/** What services need from the ledger. Implemented over DAOs in Track A (plans/track-a-backend.md). */
public interface LedgerRepository {

    Company findCompany();

    /**
     * Debit and credit totals per account for POSTED entries with start <= date <= end.
     * Only accounts with at least one such line are returned, ordered by account number.
     */
    List<AccountBalance> findPostedBalances(LocalDate start, LocalDate end);
}

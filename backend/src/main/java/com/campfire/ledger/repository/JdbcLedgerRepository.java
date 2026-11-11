package com.campfire.ledger.repository;

import com.campfire.ledger.dao.CompanyDao;
import com.campfire.ledger.dao.JournalLineDao;
import com.campfire.ledger.model.AccountBalance;
import com.campfire.ledger.model.Company;
import com.campfire.ledger.model.EntryStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLedgerRepository implements LedgerRepository {

    private final CompanyDao companyDao;
    private final JournalLineDao journalLineDao;

    public JdbcLedgerRepository(CompanyDao companyDao, JournalLineDao journalLineDao) {
        this.companyDao = companyDao;
        this.journalLineDao = journalLineDao;
    }

    @Override
    public Company findCompany() {
        return companyDao.find();
    }

    /** Draft and void entries are not in the books, so only POSTED lines are summed. */
    @Override
    public List<AccountBalance> findPostedBalances(LocalDate start, LocalDate end) {
        return journalLineDao.sumByAccount(EntryStatus.POSTED, start, end);
    }
}

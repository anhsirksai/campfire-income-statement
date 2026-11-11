package com.campfire.ledger.config;

import com.campfire.ledger.dao.AccountDao;
import com.campfire.ledger.dao.CompanyDao;
import com.campfire.ledger.dao.JournalEntryDao;
import com.campfire.ledger.dao.JournalLineDao;
import com.campfire.ledger.exception.LedgerDataException;
import com.campfire.ledger.model.Account;
import com.campfire.ledger.model.AccountSubtype;
import com.campfire.ledger.model.AccountType;
import com.campfire.ledger.model.Company;
import com.campfire.ledger.model.EntryStatus;
import com.campfire.ledger.model.JournalEntry;
import com.campfire.ledger.model.JournalLine;
import com.campfire.ledger.validation.ValidationRegistry;
import com.campfire.validation.api.LedgerSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads data/ledger.json into H2 at startup: read → validate → insert.
 * Every entry is stored, including draft and void, so the audit trail is complete;
 * the statement query is what filters to posted entries.
 * If the file is unreadable or fails a ledger rule the app does not start: wrong books must not produce numbers.
 */
@Component
public class LedgerDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LedgerDataLoader.class);

    private final ObjectMapper objectMapper;
    private final ValidationRegistry validationRegistry;
    private final CompanyDao companyDao;
    private final AccountDao accountDao;
    private final JournalEntryDao journalEntryDao;
    private final JournalLineDao journalLineDao;
    private final Path ledgerFile;

    public LedgerDataLoader(ObjectMapper objectMapper,
                            ValidationRegistry validationRegistry,
                            CompanyDao companyDao,
                            AccountDao accountDao,
                            JournalEntryDao journalEntryDao,
                            JournalLineDao journalLineDao,
                            @Value("${app.ledger-file}") Path ledgerFile) {
        this.objectMapper = objectMapper;
        this.validationRegistry = validationRegistry;
        this.companyDao = companyDao;
        this.accountDao = accountDao;
        this.journalEntryDao = journalEntryDao;
        this.journalLineDao = journalLineDao;
        this.ledgerFile = ledgerFile;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LedgerFile file = read();
        validate(file);

        companyDao.insert(new Company(file.company(), file.currency()));
        List<Account> accounts = file.accounts().stream().map(LedgerDataLoader::toAccount).toList();
        accounts.forEach(accountDao::insert);

        List<JournalEntry> entries = file.journalEntries().stream().map(LedgerDataLoader::toEntry).toList();
        int lineCount = 0;
        for (JournalEntry entry : entries) {
            journalEntryDao.insert(entry);
            for (int i = 0; i < entry.lines().size(); i++) {
                journalLineDao.insert(entry.id(), i + 1, entry.lines().get(i));
                lineCount++;
            }
        }
        log.info("Loaded {} from {}: {} accounts, {} journal entries, {} lines",
                file.company(), ledgerFile, accounts.size(), entries.size(), lineCount);
    }

    LedgerFile read() {
        try {
            return objectMapper.readValue(ledgerFile.toFile(), LedgerFile.class);
        } catch (IOException e) {
            throw new LedgerDataException("Could not read ledger file " + ledgerFile.toAbsolutePath(), e);
        }
    }

    /** Runs every ledger rule from the validation plugin. Runs before any insert. */
    void validate(LedgerFile file) {
        List<String> errors = validationRegistry.validate(toSnapshot(file));
        if (!errors.isEmpty()) {
            throw new LedgerDataException(errors);
        }
    }

    private static LedgerSnapshot toSnapshot(LedgerFile file) {
        List<LedgerSnapshot.Account> accounts = file.accounts().stream()
                .map(a -> new LedgerSnapshot.Account(a.number(), a.type(), a.subtype()))
                .toList();
        // A missing "lines" array becomes an empty list, so MinimumTwoLinesRule reports it instead of an NPE here.
        List<LedgerSnapshot.Entry> entries = file.journalEntries().stream()
                .map(e -> new LedgerSnapshot.Entry(e.id(), e.date(), e.status(),
                        (e.lines() == null ? List.<LedgerFile.LineJson>of() : e.lines()).stream()
                                .map(l -> new LedgerSnapshot.Line(l.account(), l.debit(), l.credit()))
                                .toList()))
                .toList();
        return new LedgerSnapshot(accounts, entries);
    }

    private static Account toAccount(LedgerFile.AccountJson a) {
        return new Account(a.number(), a.name(), AccountType.fromCode(a.type()),
                AccountSubtype.fromCode(a.subtype()), a.isActive());
    }

    private static JournalEntry toEntry(LedgerFile.EntryJson e) {
        List<JournalLine> lines = e.lines().stream()
                .map(l -> new JournalLine(l.account(), l.debit(), l.credit()))
                .toList();
        return new JournalEntry(e.id(), e.date(), EntryStatus.fromCode(e.status()), e.memo(), lines);
    }
}

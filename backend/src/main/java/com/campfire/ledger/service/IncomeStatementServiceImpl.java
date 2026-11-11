package com.campfire.ledger.service;

import com.campfire.ledger.model.AccountBalance;
import com.campfire.ledger.model.Company;
import com.campfire.ledger.model.StatementSection;
import com.campfire.ledger.model.dto.IncomeStatementResponse;
import com.campfire.ledger.model.dto.LineDto;
import com.campfire.ledger.model.dto.SectionDto;
import com.campfire.ledger.repository.LedgerRepository;
import com.campfire.ledger.strategy.NormalBalanceStrategyFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IncomeStatementServiceImpl implements IncomeStatementService {

    private static final Logger log = LoggerFactory.getLogger(IncomeStatementServiceImpl.class);
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final LedgerRepository ledgerRepository;

    public IncomeStatementServiceImpl(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public IncomeStatementResponse getIncomeStatement(LocalDate start, LocalDate end) {
        Company company = ledgerRepository.findCompany();
        List<AccountBalance> balances = ledgerRepository.findPostedBalances(start, end);

        // 1. One line per account, in the section its subtype maps to.
        Map<StatementSection, List<LineDto>> linesBySection = new EnumMap<>(StatementSection.class);
        for (StatementSection section : StatementSection.values()) {
            linesBySection.put(section, new ArrayList<>());
        }
        for (AccountBalance balance : balances) {
            Optional<StatementSection> section = StatementSection.fromSubtype(balance.account().subtype());
            if (section.isEmpty()) {
                continue;   // balance-sheet account: not on the income statement
            }
            linesBySection.get(section.get()).add(toLine(balance));
        }

        // 2. Section totals, always all four sections in display order.
        List<SectionDto> sections = new ArrayList<>();
        Map<StatementSection, BigDecimal> totals = new EnumMap<>(StatementSection.class);
        for (StatementSection section : StatementSection.values()) {
            List<LineDto> lines = linesBySection.get(section);
            BigDecimal total = sum(lines);
            totals.put(section, total);
            sections.add(new SectionDto(section, section.title(), lines, total));
        }

        // 3. Subtotals, spelled out so each accounting step is visible.
        BigDecimal grossProfit = totals.get(StatementSection.REVENUE)
                .subtract(totals.get(StatementSection.COGS));
        BigDecimal operatingIncome = grossProfit
                .subtract(totals.get(StatementSection.OPERATING_EXPENSES));
        BigDecimal netIncome = operatingIncome
                .add(totals.get(StatementSection.OTHER_INCOME));

        log.info("Income statement {}..{}: {} accounts with activity, net income {}",
                start, end, balances.size(), netIncome);
        return new IncomeStatementResponse(company.name(), company.currency(), start, end,
                sections, grossProfit, operatingIncome, netIncome);
    }

    /**
     * The account's balance in its normal direction: revenue is credit − debit, expenses debit − credit.
     * A contra-revenue account is type revenue with net debits, so it comes out negative and reduces Revenue.
     */
    private static LineDto toLine(AccountBalance balance) {
        BigDecimal amount = NormalBalanceStrategyFactory.forType(balance.account().type())
                .balance(balance.totalDebit(), balance.totalCredit())
                .setScale(2);   // no rounding mode: throws if a value ever had more than 2 decimals
        return new LineDto(balance.account().number(), balance.account().name(), amount);
    }

    private static BigDecimal sum(List<LineDto> lines) {
        BigDecimal total = ZERO;
        for (LineDto line : lines) {
            total = total.add(line.amount());
        }
        return total;
    }
}

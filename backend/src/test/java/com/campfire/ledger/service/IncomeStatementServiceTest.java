package com.campfire.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.ledger.model.StatementSection;
import com.campfire.ledger.model.dto.IncomeStatementResponse;
import com.campfire.ledger.model.dto.LineDto;
import com.campfire.ledger.model.dto.SectionDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Golden numbers against the real data/ledger.json loaded into H2.
 * Expected values were computed independently from the raw JSON (see spec.md §6 and the check-numbers skill).
 * Assertions use BigDecimal equals on purpose: it also checks the scale is exactly 2.
 */
@SpringBootTest
class IncomeStatementServiceTest {

    @Autowired
    private IncomeStatementService service;

    private IncomeStatementResponse statement(String start, String end) {
        return service.getIncomeStatement(LocalDate.parse(start), LocalDate.parse(end));
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static SectionDto section(IncomeStatementResponse s, StatementSection key) {
        return s.sections().stream().filter(sec -> sec.key() == key).findFirst().orElseThrow();
    }

    private static Optional<BigDecimal> line(IncomeStatementResponse s, String accountNumber) {
        return s.sections().stream()
                .flatMap(sec -> sec.lines().stream())
                .filter(l -> l.accountNumber().equals(accountNumber))
                .map(LineDto::amount)
                .findFirst();
    }

    @Nested
    class Q1_2026 {

        private final IncomeStatementResponse q1 = statement("2026-01-01", "2026-03-31");

        @Test
        void netIncome() {
            assertThat(q1.netIncome()).isEqualTo(money("-44480.14"));
        }

        @Test
        void subtotals() {
            assertThat(section(q1, StatementSection.REVENUE).total()).isEqualTo(money("37850.50"));
            assertThat(section(q1, StatementSection.COGS).total()).isEqualTo(money("14272.75"));
            assertThat(q1.grossProfit()).isEqualTo(money("23577.75"));
            assertThat(section(q1, StatementSection.OPERATING_EXPENSES).total()).isEqualTo(money("68100.07"));
            assertThat(q1.operatingIncome()).isEqualTo(money("-44522.32"));
            assertThat(section(q1, StatementSection.OTHER_INCOME).total()).isEqualTo(money("42.18"));
        }

        @Test
        void everyLine() {
            assertThat(section(q1, StatementSection.REVENUE).lines()).containsExactly(
                    new LineDto("4000", "Product Revenue", money("35650.75")),
                    new LineDto("4100", "Subscription Revenue", money("3000.00")),
                    new LineDto("4900", "Sales Returns & Discounts", money("-800.25")));
            assertThat(section(q1, StatementSection.COGS).lines()).containsExactly(
                    new LineDto("5000", "Cost of Goods Sold", money("14272.75")));
            assertThat(section(q1, StatementSection.OPERATING_EXPENSES).lines()).containsExactly(
                    new LineDto("6000", "Salaries", money("55500.00")),
                    new LineDto("6100", "Rent", money("9000.00")),
                    new LineDto("6200", "Software", money("1099.97")),
                    new LineDto("6300", "Marketing (legacy)", money("2500.10")));
            assertThat(section(q1, StatementSection.OTHER_INCOME).lines()).containsExactly(
                    new LineDto("7000", "Interest Income", money("42.18")));
        }

        @Test
        void contraRevenueReducesRevenue() {
            // 4900 has net debits (JE-012 return 650.25 + JE-016 discount 150.00). Adding it would give 39450.75.
            assertThat(line(q1, "4900")).contains(money("-800.25"));
            assertThat(section(q1, StatementSection.REVENUE).total()).isEqualTo(money("37850.50"));
        }

        @Test
        void draftEntryIsExcluded() {
            // JE-019 is a 5000.00 draft bonus accrual to Salaries; including it would give 60500.00.
            assertThat(line(q1, "6000")).contains(money("55500.00"));
        }

        @Test
        void vendorCreditReducesExpense() {
            // JE-018 debits 1199.97, JE-020 credits 100.00 back.
            assertThat(line(q1, "6200")).contains(money("1099.97"));
        }

        @Test
        void inactiveAccountWithPostedActivityIsReported() {
            assertThat(line(q1, "6300")).contains(money("2500.10"));
        }

        @Test
        void balanceSheetAccountsAreNotOnTheStatement() {
            for (String number : List.of("1000", "1100", "1200", "2000", "2100", "3000")) {
                assertThat(line(q1, number)).as("account %s", number).isEmpty();
            }
        }

        @Test
        void sectionsAreInStatementOrder() {
            assertThat(q1.sections()).extracting(SectionDto::key).containsExactly(
                    StatementSection.REVENUE, StatementSection.COGS,
                    StatementSection.OPERATING_EXPENSES, StatementSection.OTHER_INCOME);
        }

        @Test
        void companyAndPeriod() {
            assertThat(q1.company()).isEqualTo("Northwind Coffee Roasters");
            assertThat(q1.currency()).isEqualTo("USD");
            assertThat(q1.start()).isEqualTo(LocalDate.parse("2026-01-01"));
            assertThat(q1.end()).isEqualTo(LocalDate.parse("2026-03-31"));
        }
    }

    @Nested
    class OtherRanges {

        @Test
        void december2025OnlyHasJe001() {
            IncomeStatementResponse s = statement("2025-12-01", "2025-12-31");
            assertThat(line(s, "4000")).contains(money("5000.00"));
            assertThat(s.netIncome()).isEqualTo(money("5000.00"));
        }

        @Test
        void april2026OnlyHasJe024() {
            assertThat(statement("2026-04-01", "2026-04-30").netIncome()).isEqualTo(money("9100.00"));
        }

        @Test
        void bothEndsAreInclusive() {
            // 2026-03-31 has JE-021 (+1000.00 revenue), JE-022 (18500.00 payroll), JE-023 (+42.18 interest).
            IncomeStatementResponse s = statement("2026-03-31", "2026-03-31");
            assertThat(section(s, StatementSection.REVENUE).total()).isEqualTo(money("1000.00"));
            assertThat(section(s, StatementSection.OPERATING_EXPENSES).total()).isEqualTo(money("18500.00"));
            assertThat(s.netIncome()).isEqualTo(money("-17457.82"));
        }

        @Test
        void q1EndBoundaryExcludesApril1() {
            // JE-024 on 2026-04-01 must not leak into a range ending 2026-03-31.
            assertThat(line(statement("2026-03-01", "2026-03-31"), "4000")).contains(money("15000.00"));
        }

        @Test
        void voidEntryIsExcluded() {
            // 2026-02-03 has JE-009 (void, 8200.00) and JE-010 (posted, the same sale). Counting both gives 16400.00.
            IncomeStatementResponse s = statement("2026-02-03", "2026-02-03");
            assertThat(line(s, "4000")).contains(money("8200.00"));
            assertThat(s.netIncome()).isEqualTo(money("4920.00"));
        }

        @Test
        void rangeWithNoActivityHasFourEmptySections() {
            IncomeStatementResponse s = statement("2025-01-01", "2025-01-31");
            assertThat(s.sections()).hasSize(4).allSatisfy(sec -> {
                assertThat(sec.lines()).isEmpty();
                assertThat(sec.total()).isEqualTo(money("0.00"));
            });
            assertThat(s.grossProfit()).isEqualTo(money("0.00"));
            assertThat(s.operatingIncome()).isEqualTo(money("0.00"));
            assertThat(s.netIncome()).isEqualTo(money("0.00"));
        }
    }
}

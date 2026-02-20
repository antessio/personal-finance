package antessio.personalfinance.domain.service;

import antessio.personalfinance.domain.dto.CategorySpendingDTO;
import antessio.personalfinance.domain.dto.MonthlyDataDTO;
import antessio.personalfinance.domain.dto.ReportDataDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ReportDataService {

    private final TransactionQueryService transactionQueryService;
    private final BudgetService budgetService;

    public ReportDataService(TransactionQueryService transactionQueryService, BudgetService budgetService) {
        this.transactionQueryService = transactionQueryService;
        this.budgetService = budgetService;
    }

    public ReportDataDTO getAnnualReportData(String username, int year) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // Get summary totals
        BigDecimal totalIncome = transactionQueryService.getTotalIncome(username, startDate, endDate);
        BigDecimal totalExpenses = transactionQueryService.getTotalExpenses(username, startDate, endDate);
        BigDecimal totalSavings = transactionQueryService.getTotalSavings(username, startDate, endDate);

        // Get budget totals
        BigDecimal totalIncomeBudget = budgetService.getTotalIncome(username, year, null);
        BigDecimal totalExpensesBudget = budgetService.getTotalExpense(username, year, null);
        BigDecimal totalSavingsBudget = budgetService.getTotalSavings(username, year, null);

        // Get category breakdowns
        List<CategorySpendingDTO> incomeByCategory = transactionQueryService.getCategoryIncome(username, startDate, endDate);
        List<CategorySpendingDTO> expensesByCategory = transactionQueryService.getCategorySpending(username, startDate, endDate);
        List<CategorySpendingDTO> savingsByCategory = transactionQueryService.getCategorySavings(username, startDate, endDate);

        // Get monthly trends for annual report
        List<MonthlyDataDTO> monthlyTrends = transactionQueryService.getMonthlyBudgets(username, startDate, endDate);

        return new ReportDataDTO(
                "Annual Report " + year,
                username,
                year,
                null,
                totalIncome,
                totalExpenses,
                totalSavings,
                totalIncomeBudget,
                totalExpensesBudget,
                totalSavingsBudget,
                incomeByCategory,
                expensesByCategory,
                savingsByCategory,
                monthlyTrends
        );
    }

    public ReportDataDTO getMonthlyReportData(String username, int year, int month) {
        if (year < 1900 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get summary totals
        BigDecimal totalIncome = transactionQueryService.getTotalIncome(username, startDate, endDate);
        BigDecimal totalExpenses = transactionQueryService.getTotalExpenses(username, startDate, endDate);
        BigDecimal totalSavings = transactionQueryService.getTotalSavings(username, startDate, endDate);

        // Get budget totals
        BigDecimal totalIncomeBudget = budgetService.getTotalIncome(username, year, month);
        BigDecimal totalExpensesBudget = budgetService.getTotalExpense(username, year, month);
        BigDecimal totalSavingsBudget = budgetService.getTotalSavings(username, year, month);

        // Get category breakdowns
        List<CategorySpendingDTO> incomeByCategory = transactionQueryService.getCategoryIncome(username, startDate, endDate);
        List<CategorySpendingDTO> expensesByCategory = transactionQueryService.getCategorySpending(username, startDate, endDate);
        List<CategorySpendingDTO> savingsByCategory = transactionQueryService.getCategorySavings(username, startDate, endDate);

        // Monthly report doesn't need monthly trends
        String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        return new ReportDataDTO(
                "Monthly Report " + monthName + " " + year,
                username,
                year,
                month,
                totalIncome,
                totalExpenses,
                totalSavings,
                totalIncomeBudget,
                totalExpensesBudget,
                totalSavingsBudget,
                incomeByCategory,
                expensesByCategory,
                savingsByCategory,
                Collections.emptyList()
        );
    }
}

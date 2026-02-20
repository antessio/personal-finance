package antessio.personalfinance.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportDataDTO(
        String reportTitle,
        String username,
        Integer year,
        Integer month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal totalSavings,
        BigDecimal totalIncomeBudget,
        BigDecimal totalExpensesBudget,
        BigDecimal totalSavingsBudget,
        List<CategorySpendingDTO> incomeByCategory,
        List<CategorySpendingDTO> expensesByCategory,
        List<CategorySpendingDTO> savingsByCategory,
        List<MonthlyDataDTO> monthlyTrends) {
}

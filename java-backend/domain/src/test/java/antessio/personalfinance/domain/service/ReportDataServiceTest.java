package antessio.personalfinance.domain.service;

import antessio.personalfinance.domain.dto.CategoryDTO;
import antessio.personalfinance.domain.dto.CategorySpendingDTO;
import antessio.personalfinance.domain.dto.MonthlyDataDTO;
import antessio.personalfinance.domain.dto.ReportDataDTO;
import antessio.personalfinance.domain.model.CategoryId;
import antessio.personalfinance.domain.model.MacroCategoryEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportDataServiceTest {

    @Mock
    private TransactionQueryService transactionQueryService;

    @Mock
    private BudgetService budgetService;

    private ReportDataService reportDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reportDataService = new ReportDataService(transactionQueryService, budgetService);
    }

    @Test
    void shouldGenerateAnnualReportData() {
        // Given
        String username = "testuser";
        int year = 2024;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        when(transactionQueryService.getTotalIncome(username, startDate, endDate))
                .thenReturn(BigDecimal.valueOf(50000));
        when(transactionQueryService.getTotalExpenses(username, startDate, endDate))
                .thenReturn(BigDecimal.valueOf(30000));
        when(transactionQueryService.getTotalSavings(username, startDate, endDate))
                .thenReturn(BigDecimal.valueOf(10000));

        when(budgetService.getTotalIncome(username, year, null))
                .thenReturn(BigDecimal.valueOf(48000));
        when(budgetService.getTotalExpense(username, year, null))
                .thenReturn(BigDecimal.valueOf(32000));
        when(budgetService.getTotalSavings(username, year, null))
                .thenReturn(BigDecimal.valueOf(12000));

        CategoryDTO testCategory = new CategoryDTO(new CategoryId(1L), "Test", MacroCategoryEnum.EXPENSE,
                null, null, username, Collections.emptySet(), null, null);
        when(transactionQueryService.getCategoryIncome(username, startDate, endDate))
                .thenReturn(Collections.emptyList());
        when(transactionQueryService.getCategorySpending(username, startDate, endDate))
                .thenReturn(List.of(new CategorySpendingDTO(testCategory, BigDecimal.valueOf(1000), BigDecimal.valueOf(1200))));
        when(transactionQueryService.getCategorySavings(username, startDate, endDate))
                .thenReturn(Collections.emptyList());

        when(transactionQueryService.getMonthlyBudgets(username, startDate, endDate))
                .thenReturn(List.of(
                        new MonthlyDataDTO(YearMonth.of(2024, 1), 0,
                                BigDecimal.valueOf(4000), BigDecimal.valueOf(2500), BigDecimal.valueOf(800),
                                BigDecimal.valueOf(4000), BigDecimal.valueOf(2667), BigDecimal.valueOf(1000))
                ));

        // When
        ReportDataDTO result = reportDataService.getAnnualReportData(username, year);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.reportTitle()).isEqualTo("Annual Report 2024");
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.year()).isEqualTo(year);
        assertThat(result.month()).isNull();
        assertThat(result.totalIncome()).isEqualTo(BigDecimal.valueOf(50000));
        assertThat(result.totalExpenses()).isEqualTo(BigDecimal.valueOf(30000));
        assertThat(result.totalSavings()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(result.totalIncomeBudget()).isEqualTo(BigDecimal.valueOf(48000));
        assertThat(result.totalExpensesBudget()).isEqualTo(BigDecimal.valueOf(32000));
        assertThat(result.totalSavingsBudget()).isEqualTo(BigDecimal.valueOf(12000));
        assertThat(result.monthlyTrends()).hasSize(1);
        assertThat(result.expensesByCategory()).hasSize(1);

        verify(transactionQueryService).getTotalIncome(username, startDate, endDate);
        verify(transactionQueryService).getTotalExpenses(username, startDate, endDate);
        verify(transactionQueryService).getTotalSavings(username, startDate, endDate);
    }

    @Test
    void shouldGenerateMonthlyReportData() {
        // Given
        String username = "testuser";
        int year = 2024;
        int month = 6;
        LocalDate startDate = LocalDate.of(2024, 6, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 30);

        when(transactionQueryService.getTotalIncome(username, startDate, endDate))
                .thenReturn(BigDecimal.valueOf(4000));
        when(transactionQueryService.getTotalExpenses(username, startDate, endDate))
                .thenReturn(BigDecimal.valueOf(2500));
        when(transactionQueryService.getTotalSavings(username, startDate, endDate))
                .thenReturn(BigDecimal.valueOf(800));

        when(budgetService.getTotalIncome(username, year, month))
                .thenReturn(BigDecimal.valueOf(4000));
        when(budgetService.getTotalExpense(username, year, month))
                .thenReturn(BigDecimal.valueOf(2667));
        when(budgetService.getTotalSavings(username, year, month))
                .thenReturn(BigDecimal.valueOf(1000));

        when(transactionQueryService.getCategoryIncome(username, startDate, endDate))
                .thenReturn(Collections.emptyList());
        when(transactionQueryService.getCategorySpending(username, startDate, endDate))
                .thenReturn(Collections.emptyList());
        when(transactionQueryService.getCategorySavings(username, startDate, endDate))
                .thenReturn(Collections.emptyList());

        // When
        ReportDataDTO result = reportDataService.getMonthlyReportData(username, year, month);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.reportTitle()).isEqualTo("Monthly Report June 2024");
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.year()).isEqualTo(year);
        assertThat(result.month()).isEqualTo(month);
        assertThat(result.totalIncome()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(result.totalExpenses()).isEqualTo(BigDecimal.valueOf(2500));
        assertThat(result.totalSavings()).isEqualTo(BigDecimal.valueOf(800));
        assertThat(result.monthlyTrends()).isEmpty();

        verify(transactionQueryService).getTotalIncome(username, startDate, endDate);
        verify(budgetService).getTotalIncome(username, year, month);
    }

    @Test
    void shouldThrowExceptionForInvalidYear() {
        // Given
        String username = "testuser";
        int invalidYear = 1800;

        // When/Then
        assertThatThrownBy(() -> reportDataService.getAnnualReportData(username, invalidYear))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid year");
    }

    @Test
    void shouldThrowExceptionForInvalidMonth() {
        // Given
        String username = "testuser";
        int year = 2024;
        int invalidMonth = 13;

        // When/Then
        assertThatThrownBy(() -> reportDataService.getMonthlyReportData(username, year, invalidMonth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid month");
    }

    @Test
    void shouldAcceptValidYearRange() {
        // Given
        String username = "testuser";
        int year = 2000;

        when(transactionQueryService.getTotalIncome(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionQueryService.getTotalExpenses(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionQueryService.getTotalSavings(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(budgetService.getTotalIncome(anyString(), anyInt(), any())).thenReturn(BigDecimal.ZERO);
        when(budgetService.getTotalExpense(anyString(), anyInt(), any())).thenReturn(BigDecimal.ZERO);
        when(budgetService.getTotalSavings(anyString(), anyInt(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionQueryService.getCategoryIncome(any(), any(), any())).thenReturn(Collections.emptyList());
        when(transactionQueryService.getCategorySpending(any(), any(), any())).thenReturn(Collections.emptyList());
        when(transactionQueryService.getCategorySavings(any(), any(), any())).thenReturn(Collections.emptyList());
        when(transactionQueryService.getMonthlyBudgets(any(), any(), any())).thenReturn(Collections.emptyList());

        // When/Then
        assertThat(reportDataService.getAnnualReportData(username, year)).isNotNull();
        assertThat(reportDataService.getAnnualReportData(username, 1900)).isNotNull();
        assertThat(reportDataService.getAnnualReportData(username, 2100)).isNotNull();
    }
}

package antessio.personalfinance.domain.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyDataDTO(YearMonth yearMonth,
                             Integer week,
                             BigDecimal totalIncome,
                             BigDecimal totalExpenses,
                             BigDecimal totalSavings) {
}

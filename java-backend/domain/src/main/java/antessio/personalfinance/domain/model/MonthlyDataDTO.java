package antessio.personalfinance.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyDataDTO(YearMonth yearMonth,
                             BigDecimal totalIncome,
                             BigDecimal totalExpenses,
                             BigDecimal totalSavings) {
}

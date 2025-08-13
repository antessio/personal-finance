package antessio.personalfinance.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.YearMonth;

@AllArgsConstructor
@Getter
public class MonthlyBudget {
    private BudgetId id;
    private CategoryId categoryId;
    private BigDecimal amount;
    private YearMonth yearMonth;


}

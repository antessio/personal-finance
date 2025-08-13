package antessio.personalfinance.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class Budget {
    private BudgetId id;
    private CategoryId categoryId;
    private BigDecimal amount;

}

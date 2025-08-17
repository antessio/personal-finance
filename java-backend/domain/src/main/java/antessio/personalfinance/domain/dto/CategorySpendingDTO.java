package antessio.personalfinance.domain.dto;

import java.math.BigDecimal;

public record CategorySpendingDTO(CategoryDTO category,
                                 BigDecimal totalSpent,
                                 BudgetDTO budget) {
}

package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.Budget;
import antessio.personalfinance.domain.model.BudgetId;
import antessio.personalfinance.domain.model.BudgetType;
import antessio.personalfinance.domain.model.CategoryId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class BudgetDTO {
    private BudgetId budgetId;
    private CategoryId categoryId;
    private BigDecimal amount;
    private Integer year;
    private Integer month;
    private BudgetType type;

    public static BudgetDTO from(Budget budget) {
        return new BudgetDTO(
                budget.getId(),
                budget.getCategoryId(),
                budget.getAmount(),
                budget.getYear(),
                budget.getMonth(),
                budget.getBudgetType()
        );
    }

}

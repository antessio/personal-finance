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
    private String userOwner;
    private Integer year;
    private Integer month;


    public BudgetType getBudgetType(){
        if (year!= null && month != null) {
            return BudgetType.MONTHLY;
        } else if (year != null) {
            return BudgetType.YEARLY;
        } else {
            return BudgetType.DEFAULT;
        }
    }

}

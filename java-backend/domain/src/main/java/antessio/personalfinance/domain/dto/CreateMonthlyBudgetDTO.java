package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.CategoryId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.YearMonth;

@AllArgsConstructor
@Getter
public class CreateMonthlyBudgetDTO {
    private String userOwner;
    private CategoryId categoryId;
    private BigDecimal amount;
    private YearMonth yearMonth;

}

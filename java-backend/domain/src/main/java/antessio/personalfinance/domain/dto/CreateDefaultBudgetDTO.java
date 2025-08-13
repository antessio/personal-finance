package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.CategoryId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class CreateDefaultBudgetDTO {
    private String userOwner;
    private CategoryId categoryId;
    private BigDecimal amount;

}

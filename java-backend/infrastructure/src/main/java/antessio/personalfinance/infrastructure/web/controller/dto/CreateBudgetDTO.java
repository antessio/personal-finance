package antessio.personalfinance.infrastructure.web.controller.dto;

import java.math.BigDecimal;

public record CreateBudgetDTO(BigDecimal amount, Long categoryId, String yearMonth) {

    public CreateBudgetDTO {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (categoryId == null ) {
            throw new IllegalArgumentException("Category ID must not be null or blank");
        }
    }
}

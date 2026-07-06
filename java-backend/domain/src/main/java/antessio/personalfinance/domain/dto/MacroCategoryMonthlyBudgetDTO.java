package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.MacroCategoryEnum;

import java.math.BigDecimal;

public record MacroCategoryMonthlyBudgetDTO(int year, int month, MacroCategoryEnum macroCategory, BigDecimal actual, BigDecimal budget) {
}

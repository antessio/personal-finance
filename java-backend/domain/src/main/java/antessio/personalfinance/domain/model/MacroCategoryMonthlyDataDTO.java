package antessio.personalfinance.domain.model;

import java.math.BigDecimal;

public record MacroCategoryMonthlyDataDTO(int year, int month, MacroCategoryEnum macroCategory, BigDecimal total) {
}

package antessio.personalfinance.domain.model;

import java.math.BigDecimal;

public record MacroCategoryMonthlyDataDTO(int year, int month, int week, MacroCategoryEnum macroCategory, BigDecimal total) {
}

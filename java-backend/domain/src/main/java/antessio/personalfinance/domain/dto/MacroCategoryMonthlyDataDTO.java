package antessio.personalfinance.domain.dto;

import antessio.personalfinance.domain.model.MacroCategoryEnum;

import java.math.BigDecimal;

public record MacroCategoryMonthlyDataDTO(int year, int month, int week, MacroCategoryEnum macroCategory, BigDecimal total) {
}

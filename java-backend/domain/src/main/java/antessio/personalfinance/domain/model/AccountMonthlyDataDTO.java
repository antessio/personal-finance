package antessio.personalfinance.domain.model;

import java.math.BigDecimal;

public record AccountMonthlyDataDTO(int year, int month, int week, AccountType accountType, BigDecimal total) {
}
